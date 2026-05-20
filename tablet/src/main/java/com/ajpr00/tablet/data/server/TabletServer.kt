package com.ajpr00.tablet.data.server

import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.data.useCase.ImportMediaFromServerUseCase
import com.ajpr00.data.util.ImageUtils
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class TabletServer @Inject constructor(
    private val repository: MediaRepository,
    private val importMediaFromServerUseCase: ImportMediaFromServerUseCase,
) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {

        val uri = session.uri

        return when {

            uri == "/ping" -> {
                jsonResponse("""{"status":"OK"}""")
            }
            // Todo: Establcer para que obtenga LISTAS DE REPRODUCION, Cuando se implemte tener lista de reporducion
            uri == "/list_media" -> {
                val list = repository.getAllSync()
                val json = Json.encodeToString(list)
                jsonResponse(json)
            }

            uri.startsWith("/delete/") -> {
                val id = uri.removePrefix("/delete/").toIntOrNull()
                if (id != null) {
                    repository.deleteSync(id)
                    jsonResponse("""{"deleted":$id}""")
                } else {
                    jsonResponse("""{"error":"invalid id"}""", Response.Status.BAD_REQUEST)
                }
            }

            uri.startsWith("/media/") -> {
                val id = uri.removePrefix("/media/").toIntOrNull()
                println("DEBUG → id=$id")

                if (id != null) {
                    val media = repository.getMediaById(id)
                    println("DEBUG → media=$media")
                    println("DEBUG → path=${media?.path}")

                    if (media != null) {
                        val file = File(media.path)
                        println("DEBUG → fileExists=${file.exists()}")
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
                    jsonResponse("""{"error":"image not found"}""", Response.Status.NOT_FOUND)
                } else {
                    jsonResponse("""{"error":"invalid id"}""", Response.Status.BAD_REQUEST)
                }
            }

            uri.startsWith("/thumbnail/") -> {
                val id = uri.removePrefix("/thumbnail/").toIntOrNull()

                if (id != null) {
                    val media = repository.getMediaById(id)
                    if (media != null) {
                        val file = File(media.path)
                        if (file.exists()) {

                            val thumbFile = ImageUtils.generateThumbnail(file)

                            return newFixedLengthResponse(
                                Response.Status.OK,
                                "image/jpeg",
                                thumbFile.inputStream(),
                                thumbFile.length()
                            )
                        }
                    }
                    jsonResponse("""{"error":"thumbnail not found"}""", Response.Status.NOT_FOUND)
                } else {
                    jsonResponse("""{"error":"invalid id"}""", Response.Status.BAD_REQUEST)
                }
            }


            //**********************Subida*******************************

            uri == "/upload" && session.method == Method.POST -> {

                val files = HashMap<String, String>()
                session.parseBody(files)

                val tempPath = files["file"] ?: return jsonResponse("""{"error":"file missing"}""")
                val tempFile = File(tempPath)

                val mime = session.headers["content-type"] ?: "application/octet-stream"

                // Nombre original del archivo subido
                val originalName = session.parms["file"] ?: "uploaded_file"
                val extension = originalName.substringAfterLast('.', "")

                runBlocking {
                    importMediaFromServerUseCase(tempFile, mime, extension)
                }

                jsonResponse("""{"uploaded":"ok"}""")
            }

            else -> {
                jsonResponse("""{"error":"not found"}""", Response.Status.NOT_FOUND)
            }
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
