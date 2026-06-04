package com.ajpr00.tablet.data.server

import android.util.Log
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.api.*
import com.ajpr00.core.domain.usecase.media.DeleteMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetAllMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetMediaByIdSyncUseCase
import com.ajpr00.data.useCase.ImportMediaFromServerUseCase
import com.ajpr00.data.util.ImageUtils
import com.ajpr00.data.util.VideoUtils
import com.ajpr00.tablet.data.mapper.toRemote
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

/**
 * NanoHTTPD trabaja de forma síncrona: cada petición usa 1 hilo del pool (≈10 hilos).
 * Con runBlocking se bloquea ese hilo del servidor: el sistema no puede pausarlo, moverlo ni liberarlo.
 * El servidor sigue siendo síncrono; runBlocking solo adapta corrutinas a ejecución bloqueante.
 *
 * Con corrutinas el sistema puede pausar, mover y liberar; con runBlocking el hilo queda bloqueado y el sistema pierde ese privilegio
 */

class TabletServer @Inject constructor(
    private val getMediaByIdSync : GetMediaByIdSyncUseCase,
    private val deleteMediaSyncUseCase: DeleteMediaSyncUseCase,
    private val getAllMediaSyncUseCase: GetAllMediaSyncUseCase,
    private val importMediaFromServerUseCase: ImportMediaFromServerUseCase,
) : NanoHTTPD(8080) {

    private val gson = Gson()
    private val TAG = "TabletServer"

    override fun serve(session: IHTTPSession): Response {

        return try {

            val uri = session.uri
            Log.d(TAG, "→ Petición recibida: $uri")

            when {
                uri == "/ping" -> {
                    jsonResponse("OK")
                }

                uri == "/info" -> {
                    try {
                        val info = DeviceInfo(
                            id = "tablet-123",
                            name = "Tablet del Salón",
                            port = 8080,
                        )

                        val response = ApiResponse(
                            status = "OK",
                            data = info
                        )

                        jsonResponse(gson.toJson(response))

                    } catch (e: Exception) {
                        Log.e(TAG, "Error en /info: ${e.message}")
                        jsonResponse("""{"status":"ERROR","error":"internal error"}""",
                            Response.Status.INTERNAL_ERROR)
                    }
                }

                uri == "/list_media" -> {
                    try {
                        val list = getAllMediaSyncUseCase().map { it.toRemote() }
                        val response = ApiResponse(
                            status = "OK",
                            data = ListMediaData(items = list)
                        )

                        jsonResponse(gson.toJson(response))

                    } catch (e: Exception) {
                        Log.e(TAG, "Error en /list_media: ${e.message}")
                        jsonResponse("""{"status":"ERROR","error":"internal error"}""",
                            Response.Status.INTERNAL_ERROR)
                    }
                }

                uri.startsWith("/delete/") -> {
                    try {
                        val id = uri.removePrefix("/delete/")

                        if (id.isBlank()) {
                            Log.e(TAG, "ID inválido en /delete")
                            val error = ApiResponse<DeleteResult>(
                                status = "ERROR",
                                error = "invalid id"
                            )
                            return jsonResponse(gson.toJson(error), Response.Status.BAD_REQUEST)
                        }

                        deleteMediaSyncUseCase(id)

                        val response = ApiResponse(
                            status = "OK",
                            data = DeleteResult(deleted = id)
                        )

                        jsonResponse(gson.toJson(response))

                    } catch (e: Exception) {
                        Log.e(TAG, "Error en /delete: ${e.message}")
                        jsonResponse("""{"status":"ERROR","error":"internal error"}""",
                            Response.Status.INTERNAL_ERROR)
                    }
                }

                uri.startsWith("/media/") -> {
                    try {
                        val id = uri.removePrefix("/media/")

                        if (id.isBlank()) {
                            Log.e(TAG, "ID inválido en /media")
                            return jsonResponse("""{"error":"invalid id"}""",
                                Response.Status.BAD_REQUEST)
                        }

                        val media = getMediaByIdSync(id)
                        if (media != null) {
                            val file = File(media.path)
                            if (file.exists()) {
                                val mime = getMimeType(file)
                                return newFixedLengthResponse(
                                    Response.Status.OK,
                                    mime,
                                    file.inputStream(),
                                    file.length()
                                )
                            }
                        }

                        jsonResponse("""{"error":"image not found"}""",
                            Response.Status.NOT_FOUND)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error en /media: ${e.message}")
                        jsonResponse("""{"error":"internal error"}""",
                            Response.Status.INTERNAL_ERROR)
                    }
                }

                uri.startsWith("/thumbnail/") -> {
                    try {
                        val id = uri.removePrefix("/thumbnail/")
                        val media = getMediaByIdSync(id)

                        if (media != null) {
                            val file = File(media.path)

                            if (file.exists()) {

                                val thumbFile = when (media.type) {
                                    FormatType.IMAGE -> ImageUtils.generateThumbnail(file)
                                    FormatType.VIDEO -> VideoUtils.generateVideoThumbnail(file)
                                    else -> return jsonResponse("""{"error":"unsupported type"}""")
                                }

                                return newFixedLengthResponse(
                                    Response.Status.OK,
                                    "image/jpeg",
                                    thumbFile.inputStream(),
                                    thumbFile.length()
                                )
                            }
                        }

                        return jsonResponse("""{"error":"not found"}""")

                    } catch (e: Exception) {
                        e.printStackTrace()
                        return jsonResponse("""{"error":"internal error"}""")
                    }
                }


                uri == "/upload" && session.method == Method.POST -> {
                    try {
                        val files = HashMap<String, String>()
                        session.parseBody(files)

                        val tempPath = files["file"]
                            ?: return jsonResponse("""{"error":"file missing"}""")

                        val tempFile = File(tempPath)
                        val mime = session.headers["content-type"] ?: "application/octet-stream"

                        val originalName = session.parameters["file"]?.firstOrNull() ?: "uploaded_file"
                        val extension = originalName.substringAfterLast('.', "")

                        runBlocking {
                            importMediaFromServerUseCase(tempFile, mime, extension)
                        }

                        val response = ApiResponse(
                            status = "OK",
                            data = UploadResult(uploaded = true)
                        )

                        jsonResponse(gson.toJson(response))

                    } catch (e: Exception) {
                        Log.e(TAG, "Error en /upload: ${e.message}")
                        jsonResponse("""{"error":"internal error"}""",
                            Response.Status.INTERNAL_ERROR)
                    }
                }

                else -> {
                    Log.e(TAG, "Ruta no encontrada: $uri")
                    val error = ApiResponse<Unit>(
                        status = "ERROR",
                        error = "not found"
                    )
                    jsonResponse(gson.toJson(error), Response.Status.NOT_FOUND)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error general en serve(): ${e.message}")
            jsonResponse("""{"status":"ERROR","error":"internal server error"}""",
                Response.Status.INTERNAL_ERROR)
        }
    }

    private fun jsonResponse(
        body: String,
        status: Response.Status = Response.Status.OK
    ): Response {
        return newFixedLengthResponse(status, "application/json", body)
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
