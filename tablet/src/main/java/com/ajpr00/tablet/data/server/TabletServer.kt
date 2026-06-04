package com.ajpr00.tablet.data.server

import android.util.Log
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.api.*
import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.core.domain.usecase.media.DeleteMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetAllMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetMediaByIdSyncUseCase
import com.ajpr00.data.useCase.ImportMediaFromServerUseCase
import com.ajpr00.data.util.ImageUtils
import com.ajpr00.data.util.VideoUtils
import com.ajpr00.tablet.data.mapper.toRemote
import com.ajpr00.tablet.data.network.MdnsPublisher   // 👈 IMPORTANTE
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

/**
 * TabletServer
 * ---------------------------------------------------------
 * Este es el servidor HTTP que vive dentro de la tablet.
 * Aquí es donde NanoHTTPD escucha peticiones en el puerto 8080.
 *
 * Ahora también será el encargado de arrancar mDNS,
 * para que la tablet se anuncie sola en la red.
 */
class TabletServer @Inject constructor(
    private val getMediaByIdSync: GetMediaByIdSyncUseCase,
    private val deleteMediaSyncUseCase: DeleteMediaSyncUseCase,
    private val getAllMediaSyncUseCase: GetAllMediaSyncUseCase,
    private val importMediaFromServerUseCase: ImportMediaFromServerUseCase,
    private val mdnsPublisher: MdnsPublisher,
    private val prefsRepository: PreferencesRepository
) : NanoHTTPD(8080) {

    private var isClientConnected = false
    private var lastRequestTime = System.currentTimeMillis()


    private val gson = Gson()
    private val TAG = "TabletServer"

    /**
     * start()
     * ---------------------------------------------------------
     * Esto se ejecuta cuando el servidor arranca.
     * Aquí aprovechamos para encender mDNS.
     */
    override fun start() {
        super.start()
        Log.d(TAG, "Servidor HTTP iniciado en el puerto 8080")

        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }

        mdnsPublisher.start(id = id, name = name, port = 8080)

        Thread {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = now - lastRequestTime

                if (isClientConnected && diff > 10_000) { // 10 segundos sin peticiones
                    Log.d(TAG, "⏳ Cliente desconectado → reactivando mDNS")
                    isClientConnected = false
                    mdnsPublisher.start(id = id, name = name, port = 8080)
                }

                Thread.sleep(2000)
            }
        }.start()
    }


    /**
     * stop()
     * ---------------------------------------------------------
     * Esto se ejecuta cuando el servidor se detiene.
     * Aquí apagamos mDNS.
     */
    override fun stop() {
        Log.d(TAG, "Deteniendo mDNS…")
        mdnsPublisher.stop()

        Log.d(TAG, "Deteniendo servidor NanoHTTPD…")
        super.stop()
    }

    /**
     * serve()
     * ---------------------------------------------------------
     * Aquí se gestionan TODAS las rutas HTTP.
     * Cada vez que el móvil hace una petición, entra aquí.
     */
    override fun serve(session: IHTTPSession): Response {
        return try {
            lastRequestTime = System.currentTimeMillis()

            if (!isClientConnected) {
                isClientConnected = true
                Log.d(TAG, "📱 Cliente conectado → apagando mDNS")
                mdnsPublisher.stop()
            }

            val uri = session.uri
            Log.d(TAG, " Petición recibida: $uri")

            when {
                uri == "/ping" -> jsonResponse("OK")

                uri == "/info" -> {
                    Log.d(TAG, "Enviando info del dispositivo…")

                    val id = runBlocking { prefsRepository.getTabletId().first() }
                    val name = runBlocking { prefsRepository.getTabletName().first() }

                    val info = DeviceInfo(
                        id = id,
                        name = name,
                        port = 8080,
                    )
                    jsonResponse(gson.toJson(ApiResponse("OK", info)))
                }

                uri == "/list_media" -> {
                    Log.d(TAG, "Listando media…")
                    val list = getAllMediaSyncUseCase().map { it.toRemote() }
                    jsonResponse(gson.toJson(ApiResponse("OK", ListMediaData(list))))
                }

                uri.startsWith("/delete/") -> {
                    val id = uri.removePrefix("/delete/")
                    Log.d(TAG, "Eliminando media con id=$id")
                    deleteMediaSyncUseCase(id)
                    jsonResponse(gson.toJson(ApiResponse("OK", DeleteResult(id))))
                }

                uri.startsWith("/media/") -> {
                    val id = uri.removePrefix("/media/")
                    Log.d(TAG, " Descargando media con id=$id")
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
                    jsonResponse("""{"error":"not found"}""", Response.Status.NOT_FOUND)
                }

                uri.startsWith("/thumbnail/") -> {
                    val id = uri.removePrefix("/thumbnail/")
                    Log.d(TAG, "Generando thumbnail de id=$id")
                    val media = getMediaByIdSync(id)
                    if (media != null) {
                        val file = File(media.path)
                        if (file.exists()) {
                            val thumb = when (media.type) {
                                FormatType.IMAGE -> ImageUtils.generateThumbnail(file)
                                FormatType.VIDEO -> VideoUtils.generateVideoThumbnail(file)
                                else -> return jsonResponse("""{"error":"unsupported"}""")
                            }
                            return newFixedLengthResponse(
                                Response.Status.OK,
                                "image/jpeg",
                                thumb.inputStream(),
                                thumb.length()
                            )
                        }
                    }
                    jsonResponse("""{"error":"not found"}""")
                }

                uri == "/upload" && session.method == Method.POST -> {
                    Log.d(TAG, "Recibiendo archivo por /upload…")
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val tempPath =
                        files["file"] ?: return jsonResponse("""{"error":"file missing"}""")
                    val tempFile = File(tempPath)
                    val mime = session.headers["content-type"] ?: "application/octet-stream"
                    val originalName = session.parameters["file"]?.firstOrNull() ?: "uploaded_file"
                    val extension = originalName.substringAfterLast('.', "")

                    runBlocking {
                        importMediaFromServerUseCase(tempFile, mime, extension)
                    }

                    jsonResponse(gson.toJson(ApiResponse("OK", UploadResult(true))))
                }

                else -> {
                    Log.e(TAG, "Ruta no encontrada: $uri")
                    jsonResponse("""{"error":"not found"}""", Response.Status.NOT_FOUND)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error general en serve(): ${e.message}")
            jsonResponse("""{"error":"internal error"}""", Response.Status.INTERNAL_ERROR)
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
