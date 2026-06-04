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
import com.ajpr00.tablet.data.network.MdnsPublisher
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    override fun start() {
        super.start()
        Log.d(TAG, "🚀 Servidor HTTP iniciado en el puerto 8080")

        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }

        Log.d(TAG, "📡 Iniciando mDNS → id=$id name=$name port=8080")

        CoroutineScope(Dispatchers.IO).launch {
            mdnsPublisher.start(id = id, name = name, port = 8080)
            Log.d(TAG, "📡 mDNS → Publicación inicial completada")
        }

        Thread {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = now - lastRequestTime

                if (isClientConnected && diff > 10_000) {
                    Log.d(TAG, "⏳ Cliente inactivo durante $diff ms → reactivando mDNS")
                    isClientConnected = false

                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "📡 mDNS → Reactivando anuncio…")
                        mdnsPublisher.start(id = id, name = name, port = 8080)
                    }
                }

                Thread.sleep(2000)
            }
        }.start()
    }

    override fun stop() {
        Log.d(TAG, "🛑 Deteniendo mDNS…")
        mdnsPublisher.stop()

        Log.d(TAG, "🛑 Deteniendo servidor NanoHTTPD…")
        super.stop()
    }

    override fun serve(session: IHTTPSession): Response {
        return try {
            lastRequestTime = System.currentTimeMillis()

            if (!isClientConnected) {
                isClientConnected = true
                Log.d(TAG, "📱 Cliente conectado → apagando mDNS temporalmente")
                mdnsPublisher.stop()
            }

            val uri = session.uri
            val method = session.method
            val ip = session.remoteIpAddress

            Log.d(TAG, "➡️  Petición recibida: $method $uri desde $ip")

            when {

                uri == "/ping" -> {
                    Log.d(TAG, "🏓 /ping → OK")
                    jsonResponse("OK")
                }

                uri == "/info" -> {
                    Log.d(TAG, "ℹ️  /info → Enviando información del dispositivo…")

                    val id = runBlocking { prefsRepository.getTabletId().first() }
                    val name = runBlocking { prefsRepository.getTabletName().first() }

                    val info = DeviceInfo(id = id, name = name, port = 8080)
                    jsonResponse(gson.toJson(ApiResponse("OK", info)))
                }

                uri == "/list_media" -> {
                    Log.d(TAG, "📂 /list_media → Listando archivos…")
                    val list = getAllMediaSyncUseCase().map { it.toRemote() }
                    jsonResponse(gson.toJson(ApiResponse("OK", ListMediaData(list))))
                }

                uri.startsWith("/delete/") -> {
                    val id = uri.removePrefix("/delete/")
                    Log.d(TAG, "🗑️  /delete/$id → Eliminando media…")
                    deleteMediaSyncUseCase(id)
                    jsonResponse(gson.toJson(ApiResponse("OK", DeleteResult(id))))
                }

                uri.startsWith("/media/") -> {
                    val id = uri.removePrefix("/media/")
                    Log.d(TAG, "📥 /media/$id → Descargando archivo…")

                    val media = getMediaByIdSync(id)
                    if (media != null) {
                        val file = File(media.path)
                        if (file.exists()) {
                            val mime = getMimeType(file)
                            Log.d(TAG, "📤 Enviando archivo $file con mime=$mime")
                            return newFixedLengthResponse(
                                Response.Status.OK,
                                mime,
                                file.inputStream(),
                                file.length()
                            )
                        }
                    }
                    Log.e(TAG, "❌ /media/$id → Archivo no encontrado")
                    jsonResponse("""{"error":"not found"}""", Response.Status.NOT_FOUND)
                }

                uri.startsWith("/thumbnail/") -> {
                    val id = uri.removePrefix("/thumbnail/")
                    Log.d(TAG, "🖼️  /thumbnail/$id → Generando thumbnail…")

                    val media = getMediaByIdSync(id)
                    if (media != null) {
                        val file = File(media.path)
                        if (file.exists()) {
                            val thumb = when (media.type) {
                                FormatType.IMAGE -> ImageUtils.generateThumbnail(file)
                                FormatType.VIDEO -> VideoUtils.generateVideoThumbnail(file)
                                else -> {
                                    Log.e(TAG, "❌ /thumbnail/$id → Tipo no soportado")
                                    return jsonResponse("""{"error":"unsupported"}""")
                                }
                            }

                            Log.d(TAG, "📤 Enviando thumbnail (${thumb.length()} bytes)")
                            return newFixedLengthResponse(
                                Response.Status.OK,
                                "image/jpeg",
                                thumb.inputStream(),
                                thumb.length()
                            )
                        }
                    }
                    Log.e(TAG, "❌ /thumbnail/$id → Archivo no encontrado")
                    jsonResponse("""{"error":"not found"}""")
                }

                uri == "/upload" && method == Method.POST -> {
                    Log.d(TAG, "⬆️  /upload → Recibiendo archivo…")

                    val files = HashMap<String, String>()
                    session.parseBody(files)

                    val tempPath = files["file"]
                    if (tempPath == null) {
                        Log.e(TAG, "❌ /upload → Archivo no recibido")
                        return jsonResponse("""{"error":"file missing"}""")
                    }

                    val tempFile = File(tempPath)
                    val mime = session.headers["content-type"] ?: "application/octet-stream"
                    val originalName = session.parameters["file"]?.firstOrNull() ?: "uploaded_file"
                    val extension = originalName.substringAfterLast('.', "")

                    Log.d(TAG, "📦 Archivo recibido: $originalName ($mime)")

                    runBlocking {
                        importMediaFromServerUseCase(tempFile, mime, extension)
                    }

                    Log.d(TAG, "✅ /upload → Archivo importado correctamente")
                    jsonResponse(gson.toJson(ApiResponse("OK", UploadResult(true))))
                }

                else -> {
                    Log.e(TAG, "❌ Ruta no encontrada: $uri")
                    jsonResponse("""{"error":"not found"}""", Response.Status.NOT_FOUND)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error general en serve(): ${e.message}", e)
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
