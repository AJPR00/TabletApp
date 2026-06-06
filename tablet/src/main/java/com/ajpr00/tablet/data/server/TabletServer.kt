package com.ajpr00.tablet.data.server

import android.util.Log
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.api.ApiResponse
import com.ajpr00.core.domain.model.api.DeleteResult
import com.ajpr00.core.domain.model.api.DeviceInfo
import com.ajpr00.core.domain.model.api.ListMediaData
import com.ajpr00.core.domain.model.api.UploadResult
import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.core.domain.usecase.media.DeleteMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetAllMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetMediaByIdSyncUseCase
import com.ajpr00.data.crypto.Crypto
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
 *
 * Servidor HTTP embebido (NanoHTTPD) que actúa como puerta de entrada para la app móvil.
 *
 * ### Propósito
 * - Exponer endpoints REST para que el móvil pueda:
 *   - comprobar disponibilidad (`/ping`),
 *   - obtener información (`/info`),
 *   - listar media (`/list_media`),
 *   - descargar media y thumbnails (`/media/{id}`, `/thumbnail/{id}`),
 *   - eliminar media (`/delete/{id}`),
 *   - subir archivos cifrados (`/upload`).
 *
 * ### Responsabilidad dentro de la arquitectura
 * - **Infraestructura HTTP**: recibe peticiones y devuelve respuestas.
 * - **Coordinador**: valida headers, parsea multipart, descifra y delega en UseCases.
 * - **No contiene lógica de negocio**: la lógica de persistencia y reglas están en los UseCases (domain).
 *
 * ### Flujo interno relevante (upload cifrado)
 * 1. Validar token `X-Auth-Token`.
 * 2. `session.parseBody()` para obtener la ruta del archivo temporal.
 * 3. Leer bytes cifrados y descifrarlos con `Crypto`.
 * 4. Guardar un fichero temporal con los bytes descifrados.
 * 5. Llamar a `ImportMediaFromServerUseCase` para guardar en sandbox y Room.
 * 6. Responder con `ApiResponse<UploadResult>`.
 *
 * ### Notas y advertencias
 * - Todas las respuestas JSON siguen el wrapper `ApiResponse<T>` para compatibilidad con Gson/Retrofit.
 * - Se especifica el tipo genérico en cada `ApiResponse<T>` para evitar errores de inferencia.
 * - `Crypto` usa AES/GCM; si la clave es incorrecta o los datos están corruptos, el descifrado devuelve `ByteArray(0)` y la petición se rechaza.
 *
 * ### Relación con capas
 * - **presentation**: el móvil consume estos endpoints (Retrofit + Gson).
 * - **data**: usa mappers (`toRemote`) y utilidades de imagen/vídeo.
 * - **core/domain**: delega en UseCases síncronos para operaciones con Room.
 *
 * @constructor Inyecta los use cases y componentes de infraestructura necesarios.
 */
class TabletServer @Inject constructor(
    private val getMediaByIdSync: GetMediaByIdSyncUseCase,
    private val deleteMediaSyncUseCase: DeleteMediaSyncUseCase,
    private val getAllMediaSyncUseCase: GetAllMediaSyncUseCase,
    private val importMediaFromServerUseCase: ImportMediaFromServerUseCase,
    private val mdnsPublisher: MdnsPublisher,
    private val prefsRepository: PreferencesRepository
) : NanoHTTPD(8080) {

    /** Marca si hay un cliente activo para pausar mDNS temporalmente. */
    private var isClientConnected = false

    /** Última vez que se recibió una petición (ms). */
    private var lastRequestTime = System.currentTimeMillis()

    /** Instancia de cifrado AES/GCM. La clave debe gestionarse desde prefs en producción. */
    private val crypto = Crypto("CLAVE_COMPARTIDA_USUARIO")

    /** Token compartido simple para proteger `/upload`. En producción usar mecanismo más robusto. */
    private val AUTH_TOKEN = "TOKEN_SECRETO_COMPARTIDO"

    private val gson = Gson()
    private val TAG = "TabletServer"

    // ---------------------------------------------------------
    //  CICLO DE VIDA: START / STOP
    // ---------------------------------------------------------

    /**
     * Inicia NanoHTTPD y publica el servicio por mDNS.
     *
     * Flujo:
     * - Inicia el servidor HTTP.
     * - Recupera id y nombre de la tablet desde [PreferencesRepository].
     * - Publica el servicio con [MdnsPublisher].
     * - Lanza un hilo que re-publica mDNS si el cliente se desconecta por inactividad.
     *
     * Advertencias:
     * - No bloquear el hilo principal; las operaciones de red se lanzan en Dispatchers.IO.
     */
    override fun start() {
        super.start()
        Log.d(TAG, "🚀 Servidor HTTP iniciado en 8080")

        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }

        Log.d(TAG, "📡 Iniciando mDNS → id=$id name=$name port=8080")

        CoroutineScope(Dispatchers.IO).launch {
            mdnsPublisher.start(id, name, 8080)
            Log.d(TAG, "📡 mDNS → Publicación inicial completada")
        }

        // Hilo que vigila la inactividad y reactiva mDNS si el cliente desaparece.
        Thread {
            while (true) {
                val diff = System.currentTimeMillis() - lastRequestTime
                if (isClientConnected && diff > 10_000) {
                    Log.d(TAG, "⏳ Cliente inactivo ($diff ms) → reactivando mDNS")
                    isClientConnected = false
                    CoroutineScope(Dispatchers.IO).launch {
                        mdnsPublisher.start(id, name, 8080)
                        Log.d(TAG, "📡 mDNS → Reactivado tras inactividad")
                    }
                }
                Thread.sleep(2000)
            }
        }.start()
    }

    /**
     * Detiene mDNS y el servidor HTTP.
     */
    override fun stop() {
        Log.d(TAG, "🛑 Deteniendo mDNS…")
        mdnsPublisher.stop()
        Log.d(TAG, "🛑 Deteniendo servidor NanoHTTPD…")
        super.stop()
    }

    // ---------------------------------------------------------
    //  ROUTER PRINCIPAL
    // ---------------------------------------------------------

    /**
     * Router principal que despacha las peticiones HTTP.
     *
     * - Valida y actualiza `lastRequestTime`.
     * - Pausa mDNS mientras hay un cliente activo para evitar ruido en la red.
     * - Devuelve siempre `ApiResponse<T>` para endpoints JSON.
     *
     * @param session Objeto con la petición HTTP.
     * @return [NanoHTTPD.Response] con JSON o stream binario.
     */
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

            Log.d(TAG, "➡️ Petición recibida: $method $uri desde ${session.remoteIpAddress}")

            when {
                uri == "/ping" -> handlePing()
                uri == "/info" -> handleInfo()
                uri == "/list_media" -> handleListMedia()
                uri.startsWith("/delete/") -> handleDelete(uri)
                uri.startsWith("/media/") -> handleMediaDownload(uri)
                uri.startsWith("/thumbnail/") -> handleThumbnail(uri)
                uri == "/upload" && method == Method.POST -> handleEncryptedUpload(session)
                else -> json(ApiResponse<Unit>(status = "ERROR", error = "not found"), Response.Status.NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error general en serve(): ${e.message}", e)
            return json(ApiResponse<Unit>(status = "ERROR", error = "internal error"), Response.Status.INTERNAL_ERROR)
        }
    }

    // ---------------------------------------------------------
    //  ENDPOINTS (implementación)
    // ---------------------------------------------------------

    /**
     * `GET /ping`
     *
     * Respuesta simple para comprobar disponibilidad.
     *
     * @return ApiResponse<String> con `data = "pong"`.
     */
    private fun handlePing(): Response {
        Log.d(TAG, "🏓 /ping → OK")
        return json(ApiResponse<String>(status = "OK", data = "pong"))
    }

    /**
     * `GET /info`
     *
     * Devuelve id, nombre y puerto de la tablet.
     *
     * @return ApiResponse<DeviceInfo> con la información del dispositivo.
     */
    private fun handleInfo(): Response {
        Log.d(TAG, "ℹ️ /info → Enviando información del dispositivo…")
        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }
        val info = DeviceInfo(id = id, name = name, port = 8080)
        return json(ApiResponse<DeviceInfo>(status = "OK", data = info))
    }

    /**
     * `GET /list_media`
     *
     * Lista todos los media almacenados en Room.
     *
     * Flujo:
     * 1. Llamada síncrona a [GetAllMediaSyncUseCase].
     * 2. Mapeo a DTO remoto con `toRemote()`.
     * 3. Envoltorio en [ListMediaData] y [ApiResponse].
     *
     * @return ApiResponse<ListMediaData>
     */
    private fun handleListMedia(): Response {
        Log.d(TAG, "📂 /list_media → Listando archivos…")
        val list = getAllMediaSyncUseCase().map { it.toRemote() }
        return json(ApiResponse<ListMediaData>(status = "OK", data = ListMediaData(list)))
    }

    /**
     * `DELETE /delete/{id}`
     *
     * Elimina un media por id delegando en el use case.
     *
     * @param uri Ruta completa; se extrae el id.
     * @return ApiResponse<DeleteResult> con el id eliminado.
     */
    private fun handleDelete(uri: String): Response {
        val id = uri.removePrefix("/delete/")
        Log.d(TAG, "🗑️ /delete/$id → Eliminando media…")
        deleteMediaSyncUseCase(id)
        return json(ApiResponse<DeleteResult>(status = "OK", data = DeleteResult(id)))
    }

    /**
     * `GET /media/{id}`
     *
     * Devuelve el archivo original como stream binario.
     *
     * @param uri Ruta completa; se extrae el id.
     * @return Response binaria con el archivo o ApiResponse<Unit> de error.
     */
    private fun handleMediaDownload(uri: String): Response {
        val id = uri.removePrefix("/media/")
        Log.d(TAG, "📥 /media/$id → Descargando archivo…")

        val media = getMediaByIdSync(id)
            ?: return json(ApiResponse<Unit>(status = "ERROR", error = "not found"), Response.Status.NOT_FOUND)

        val file = File(media.path)
        if (!file.exists()) {
            Log.e(TAG, "❌ /media/$id → Archivo físico no encontrado")
            return json(ApiResponse<Unit>(status = "ERROR", error = "not found"), Response.Status.NOT_FOUND)
        }

        val mime = getMimeType(file)
        Log.d(TAG, "📤 Enviando archivo ${file.name} con mime=$mime")
        return newFixedLengthResponse(Response.Status.OK, mime, file.inputStream(), file.length())
    }

    /**
     * `GET /thumbnail/{id}`
     *
     * Genera y devuelve un thumbnail JPEG para imagen o vídeo.
     *
     * @param uri Ruta completa; se extrae el id.
     * @return Stream JPEG o ApiResponse<Unit> de error.
     */
    private fun handleThumbnail(uri: String): Response {
        val id = uri.removePrefix("/thumbnail/")
        Log.d(TAG, "🖼️ /thumbnail/$id → Generando thumbnail…")

        val media = getMediaByIdSync(id)
            ?: return json(ApiResponse<Unit>(status = "ERROR", error = "not found"))

        val file = File(media.path)
        if (!file.exists()) {
            Log.e(TAG, "❌ /thumbnail/$id → Archivo físico no encontrado")
            return json(ApiResponse<Unit>(status = "ERROR", error = "not found"))
        }

        val thumb = when (media.type) {
            FormatType.IMAGE -> ImageUtils.generateThumbnail(file)
            FormatType.VIDEO -> VideoUtils.generateVideoThumbnail(file)
            else -> {
                Log.e(TAG, "❌ /thumbnail/$id → Tipo no soportado: ${media.type}")
                return json(ApiResponse<Unit>(status = "ERROR", error = "unsupported"))
            }
        }

        Log.d(TAG, "📤 Enviando thumbnail (${thumb.length()} bytes)")
        return newFixedLengthResponse(Response.Status.OK, "image/jpeg", thumb.inputStream(), thumb.length())
    }

    // ---------------------------------------------------------
    //  /upload CIFRADO (detalle)
    // ---------------------------------------------------------

    /**
     * `POST /upload`
     *
     * Recibe un archivo cifrado (AES/GCM) enviado por el móvil.
     *
     * Flujo paso a paso:
     * 1. Validar header `X-Auth-Token`.
     * 2. `session.parseBody(files)` para obtener la ruta temporal del multipart.
     * 3. Leer bytes cifrados desde el fichero temporal.
     * 4. Descifrar con [Crypto.decrypt].
     * 5. Guardar fichero temporal descifrado.
     * 6. Llamar a [ImportMediaFromServerUseCase] para persistir en sandbox y Room.
     * 7. Responder `ApiResponse<UploadResult>` con success = true.
     *
     * Parámetros:
     * - `session`: sesión HTTP con headers y multipart.
     *
     * Retorno:
     * - `Response` con JSON `ApiResponse<UploadResult>` o error con tipo explícito.
     *
     * Errores comunes:
     * - Token inválido → 401.
     * - Multipart sin campo `file` → 400.
     * - Descifrado fallido (clave incorrecta o datos corruptos) → 400.
     */
    private fun handleEncryptedUpload(session: IHTTPSession): Response {
        Log.d(TAG, "⬆️ /upload → Petición de subida recibida")

        // 1. Validación de token
        val token = session.headers["x-auth-token"]
        if (token != AUTH_TOKEN) {
            Log.e(TAG, "❌ /upload → Token inválido")
            return json(ApiResponse<Unit>(status = "ERROR", error = "unauthorized"), Response.Status.UNAUTHORIZED)
        }
        Log.d(TAG, "🔐 /upload → Token válido, procesando archivo cifrado…")

        // 2. Parse multipart body (NanoHTTPD escribe un fichero temporal y devuelve su ruta)
        val files = HashMap<String, String>()
        session.parseBody(files)

        val tempPath = files["file"]
        if (tempPath == null) {
            Log.e(TAG, "❌ /upload → Archivo no recibido en multipart")
            return json(ApiResponse<Unit>(status = "ERROR", error = "file missing"), Response.Status.BAD_REQUEST)
        }

        val tempFile = File(tempPath)
        val encryptedBytes = try {
            tempFile.readBytes()
        } catch (e: Exception) {
            Log.e(TAG, "❌ /upload → Error leyendo fichero temporal: ${e.message}", e)
            return json(ApiResponse<Unit>(status = "ERROR", error = "file read error"), Response.Status.INTERNAL_ERROR)
        }
        Log.d(TAG, "📦 /upload → Bytes cifrados recibidos (${encryptedBytes.size} bytes)")

        // 3. Descifrado AES/GCM
        val decryptedBytes = crypto.decrypt(encryptedBytes)
        if (decryptedBytes.isEmpty()) {
            Log.e(TAG, "❌ /upload → Error descifrando datos (clave incorrecta o datos corruptos)")
            return json(ApiResponse<Unit>(status = "ERROR", error = "decrypt failed"), Response.Status.BAD_REQUEST)
        }

        // 4. Guardar fichero temporal descifrado
        val plainTempFile = File(tempFile.parentFile, "dec_${tempFile.name}")
        try {
            plainTempFile.writeBytes(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "❌ /upload → Error escribiendo fichero descifrado: ${e.message}", e)
            return json(ApiResponse<Unit>(status = "ERROR", error = "file write error"), Response.Status.INTERNAL_ERROR)
        }
        Log.d(TAG, "📁 /upload → Archivo descifrado guardado en ${plainTempFile.absolutePath}")

        // 5. Extraer metadata y delegar en use case
        val mime = session.headers["content-type"] ?: "application/octet-stream"
        val originalName = session.parameters["file"]?.firstOrNull() ?: "uploaded_file"
        val ext = originalName.substringAfterLast('.', "")

        Log.d(TAG, "📦 /upload → Archivo descifrado: $originalName ($mime) ext=$ext")

        runBlocking {
            importMediaFromServerUseCase(plainTempFile, mime, ext)
        }

        Log.d(TAG, "✅ /upload → Archivo importado correctamente en Room")
        return json(
            ApiResponse<UploadResult>(
                status = "OK",
                data = UploadResult(
                    success = true,
                    uploaded = true
                )
            )
        )

    }

    // ---------------------------------------------------------
    //  UTILIDADES
    // ---------------------------------------------------------

    /**
     * Serializa [ApiResponse] a JSON usando Gson y devuelve una respuesta HTTP.
     *
     * - Siempre especifica el tipo genérico en la llamada para evitar errores de inferencia.
     *
     * @param body ApiResponse con tipo explícito.
     * @param status Código HTTP a devolver (por defecto 200 OK).
     * @return [NanoHTTPD.Response] con `application/json`.
     */
    private fun json(body: ApiResponse<*>, status: Response.Status = Response.Status.OK): Response {
        val json = gson.toJson(body)
        Log.d(TAG, "↩️ Respondiendo JSON: ${body.status} (payload size ~${json.length} chars)")
        return newFixedLengthResponse(status, "application/json", json)
    }

    /**
     * Detecta el MIME de un archivo según su extensión.
     *
     * @param file Archivo físico.
     * @return Tipo MIME (por ejemplo "image/jpeg").
     */
    private fun getMimeType(file: File): String =
        when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
}
