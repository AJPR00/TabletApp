package com.ajpr00.tablet.data.server

import android.util.Base64
import android.util.Log
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.api.ApiResponse
import com.ajpr00.core.domain.model.api.DeleteResult
import com.ajpr00.core.domain.model.api.DeviceInfo
import com.ajpr00.core.domain.model.api.ListMediaData
import com.ajpr00.core.domain.model.api.UploadResult
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.core.domain.usecase.media.DeleteMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetAllMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetMediaByIdSyncUseCase
import com.ajpr00.core.security.Crypto
import com.ajpr00.core.security.Pbkdf2KeyDeriver
import com.ajpr00.core.security.PinGenerator
import com.ajpr00.core.security.SaltGenerator
import com.ajpr00.core.util.NetworkUtils
import com.ajpr00.core.util.NetworkUtils.getLocalIpAddress
import com.ajpr00.data.useCase.ImportMediaFromServerUseCase
import com.ajpr00.data.util.ImageUtils
import com.ajpr00.data.util.VideoUtils
import com.ajpr00.tablet.data.mapper.toRemote
import com.ajpr00.tablet.data.network.MdnsPublisher
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

/**
 * # TabletServer
 *
 * Servidor HTTP embebido basado en `NanoHTTPD` que actúa como puerta de entrada
 * para la app móvil dentro de la red local.
 *
 * ## Responsabilidades principales
 * - Exponer endpoints REST para:
 *   - Comprobar disponibilidad: `GET /ping`.
 *   - Obtener información del dispositivo: `GET /info`.
 *   - Listar media: `GET /list_media`.
 *   - Descargar media: `GET /media/{id}`.
 *   - Descargar thumbnails: `GET /thumbnail/{id}`.
 *   - Eliminar media: `DELETE /delete/{id}`.
 *   - Gestionar vinculación segura: `POST /show_pin`, `POST /pair`.
 *   - Recibir archivos cifrados: `POST /upload`.
 *
 * ## Seguridad
 * - La vinculación se basa en:
 *   - PIN humano (6 dígitos).
 *   - SALT aleatorio.
 *   - PBKDF2 para derivar una clave temporal.
 *   - Entrega cifrada de la clave AES REAL (AES‑128).
 * - La clave AES REAL se guarda en `PreferencesRepository` y se usa para cifrar
 *   y descifrar el tráfico real (por ejemplo, `/upload`).
 *
 * ## Relación con otras capas
 * - **domain**: delega en use cases síncronos para operaciones con Room.
 * - **data**: usa mappers (`toRemote`) y utilidades de imagen/vídeo.
 * - **core/security**: usa `Crypto`, `Pbkdf2KeyDeriver`, `SaltGenerator`, `PinGenerator`.
 * - **network**: usa `MdnsPublisher` para anunciar el servicio en la red local.
 *
 * ## Notas importantes
 * - Este servidor no contiene lógica de negocio; solo enruta y coordina.
 * - Todas las respuestas JSON usan el wrapper `ApiResponse<T>`.
 */
class TabletServer @Inject constructor(
    private val getMediaByIdSync: GetMediaByIdSyncUseCase,
    private val deleteMediaSyncUseCase: DeleteMediaSyncUseCase,
    private val getAllMediaSyncUseCase: GetAllMediaSyncUseCase,
    private val importMediaFromServerUseCase: ImportMediaFromServerUseCase,
    private val mdnsPublisher: MdnsPublisher,
    private val prefsRepository: PreferencesRepository
) : NanoHTTPD(8080) {

    private val _pinFlow = MutableSharedFlow<String>(replay = 1)
    val pinFlow = _pinFlow.asSharedFlow()

    private val _saltFlow = MutableSharedFlow<ByteArray>(replay = 1)
    val saltFlow = _saltFlow.asSharedFlow()

    /** Indica si hay un cliente activo para pausar mDNS temporalmente. */
    private var isClientConnected: Boolean = false

    /** Marca la última vez que se recibió una petición HTTP. */
    private var lastRequestTime: Long = System.currentTimeMillis()

    /** PIN actual generado para el proceso de vinculación. Solo vive en memoria. */
    private var currentPin: String? = null

    /** SALT actual asociado al PIN. Solo vive en memoria. */
    private var currentSalt: ByteArray? = null

    private val gson = Gson()
    private val TAG = "TabletServer"

    // -------------------------------------------------------------------------
    // CICLO DE VIDA: START / STOP
    // -------------------------------------------------------------------------

    /**
     * Inicia el servidor HTTP y publica el servicio por mDNS.
     *
     * Flujo:
     * 1. Llama a `super.start()` para arrancar NanoHTTPD.
     * 2. Recupera `tabletId` y `tabletName` desde [PreferencesRepository].
     * 3. Publica el servicio mDNS con [MdnsPublisher].
     * 4. Lanza un hilo que vigila la inactividad y reactiva mDNS si el cliente
     *    deja de hacer peticiones durante un tiempo.
     *
     * Advertencias:
     * - No debe ejecutarse en el hilo principal.
     * - Si `tabletId` o `tabletName` están vacíos, la publicación mDNS será
     *   menos útil; se asume que el onboarding ya se ha completado.
     */
    override fun start() {
        super.start()
        Log.d(TAG, "Servidor HTTP iniciado en el puerto 8080")

        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }

        Log.d(TAG, "Iniciando mDNS con id=$id name=$name port=8080")

        CoroutineScope(Dispatchers.IO).launch {
            mdnsPublisher.start(id, name, 8080)
            Log.d(TAG, "Publicación mDNS inicial completada")
        }

        // Hilo que vigila la inactividad y reactiva mDNS si el cliente desaparece.
        Thread {
            while (true) {
                val diff = System.currentTimeMillis() - lastRequestTime
                if (isClientConnected && diff > 10_000) {
                    Log.d(TAG, "Cliente inactivo durante $diff ms. Reactivando mDNS.")
                    isClientConnected = false
                    CoroutineScope(Dispatchers.IO).launch {
                        mdnsPublisher.start(id, name, 8080)
                        Log.d(TAG, "mDNS reactivado tras inactividad")
                    }
                }
                Thread.sleep(2000)
            }
        }.start()
    }

    /**
     * Detiene el servidor HTTP y la publicación mDNS.
     */
    override fun stop() {
        Log.d(TAG, "Deteniendo mDNS")
        mdnsPublisher.stop()
        Log.d(TAG, "Deteniendo servidor NanoHTTPD")
        super.stop()
    }

    // -------------------------------------------------------------------------
    // ROUTER PRINCIPAL
    // -------------------------------------------------------------------------

    /**
     * Router principal del servidor HTTP.
     *
     * Responsabilidades:
     * - Actualizar el timestamp de actividad.
     * - Pausar mDNS cuando se detecta un cliente activo.
     * - Registrar logs de cada petición.
     * - Despachar a los handlers según la ruta.
     *
     * Rutas soportadas:
     * - `GET /ping`
     * - `GET /info`
     * - `GET /list_media`
     * - `DELETE /delete/{id}`
     * - `GET /media/{id}`
     * - `GET /thumbnail/{id}`
     * - `POST /show_pin`
     * - `POST /pair`
     * - `POST /upload`
     */
    override fun serve(session: IHTTPSession): Response {
        return try {
            // 1. Actualizar timestamp de actividad
            lastRequestTime = System.currentTimeMillis()

            // 2. Si es la primera petición del cliente, detener mDNS
            if (!isClientConnected) {
                isClientConnected = true
                Log.d(TAG, "Cliente conectado. Pausando mDNS temporalmente.")
                mdnsPublisher.stop()
            }

            // 3. Extraer datos de la petición
            val uri = session.uri
            val method = session.method

            Log.d(TAG, "Petición recibida: $method $uri desde ${session.remoteIpAddress}")

            // 4. Router principal
            when {
                uri == "/ping" && method == Method.GET -> handlePing()
                uri == "/info" && method == Method.GET -> handleInfo()
                uri == "/list_media" && method == Method.GET -> handleListMedia()
                uri.startsWith("/delete/") && method == Method.DELETE -> handleDelete(uri)
                uri.startsWith("/media/") && method == Method.GET -> handleMediaDownload(uri)
                uri.startsWith("/thumbnail/") && method == Method.GET -> handleThumbnail(uri)
                uri == "/show_pin" && method == Method.POST -> handleShowPin()
                uri == "/pair" && method == Method.POST -> handlePair(session)
                uri == "/upload" && method == Method.POST -> handleEncryptedUpload(session)
                else -> {
                    Log.w(TAG, "Ruta no encontrada: $uri")
                    json(
                        ApiResponse<Unit>(
                            status = "ERROR",
                            error = "not found"
                        ),
                        Response.Status.NOT_FOUND
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en serve(): ${e.message}", e)
            json(
                ApiResponse<Unit>(
                    status = "ERROR",
                    error = "internal error"
                ),
                Response.Status.INTERNAL_ERROR
            )
        }
    }

    // -------------------------------------------------------------------------
    // ENDPOINTS BÁSICOS
    // -------------------------------------------------------------------------

    /**
     * `GET /ping`
     *
     * Endpoint simple para comprobar que el servidor está vivo.
     *
     * @return `ApiResponse<String>` con `data = "pong"`.
     */
    private fun handlePing(): Response {
        Log.d(TAG, "/ping → OK")
        return json(ApiResponse(status = "OK", data = "pong"))
    }

    /**
     * `GET /info`
     *
     * Devuelve información básica de la tablet:
     * - id
     * - nombre
     * - puerto
     */
    private fun handleInfo(): Response {
        Log.d(TAG, "/info → Enviando información del dispositivo")
        val id = runBlocking { prefsRepository.getTabletId().first() }
        val name = runBlocking { prefsRepository.getTabletName().first() }
        val info = DeviceInfo(id = id, name = name, port = 8080)
        return json(ApiResponse(status = "OK", data = info))
    }

    /**
     * `GET /list_media`
     *
     * Lista todos los media almacenados en Room.
     *
     * Flujo:
     * 1. Llama a [GetAllMediaSyncUseCase].
     * 2. Mapea a DTO remoto con `toRemote()`.
     * 3. Envuelve en [ListMediaData] y [ApiResponse].
     */
    private fun handleListMedia(): Response {
        Log.d(TAG, "/list_media → Listando archivos")
        val list = getAllMediaSyncUseCase().map { it.toRemote() }
        return json(ApiResponse(status = "OK", data = ListMediaData(list)))
    }

    /**
     * `DELETE /delete/{id}`
     *
     * Elimina un media por id.
     */
    private fun handleDelete(uri: String): Response {
        val id = uri.removePrefix("/delete/")
        Log.d(TAG, "/delete/$id → Eliminando media")
        deleteMediaSyncUseCase(id)
        return json(ApiResponse(status = "OK", data = DeleteResult(id)))
    }

    /**
     * `GET /media/{id}`
     *
     * Devuelve el archivo original como stream binario.
     */
    private fun handleMediaDownload(uri: String): Response {
        val id = uri.removePrefix("/media/")
        Log.d(TAG, "/media/$id → Descargando archivo")

        val media = getMediaByIdSync(id)
            ?: return json(
                ApiResponse<Unit>(status = "ERROR", error = "not found"),
                Response.Status.NOT_FOUND
            )

        val file = File(media.path)
        if (!file.exists()) {
            Log.e(TAG, "/media/$id → Archivo físico no encontrado")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "not found"),
                Response.Status.NOT_FOUND
            )
        }

        val mime = getMimeType(file)
        Log.d(TAG, "Enviando archivo ${file.name} con mime=$mime")
        return newFixedLengthResponse(Response.Status.OK, mime, file.inputStream(), file.length())
    }

    /**
     * `GET /thumbnail/{id}`
     *
     * Genera y devuelve un thumbnail JPEG para imagen o vídeo.
     */
    private fun handleThumbnail(uri: String): Response {
        val id = uri.removePrefix("/thumbnail/")
        Log.d(TAG, "/thumbnail/$id → Generando thumbnail")

        val media = getMediaByIdSync(id)
            ?: return json(ApiResponse<Unit>(status = "ERROR", error = "not found"))

        val file = File(media.path)
        if (!file.exists()) {
            Log.e(TAG, "/thumbnail/$id → Archivo físico no encontrado")
            return json(ApiResponse<Unit>(status = "ERROR", error = "not found"))
        }

        val thumb = when (media.type) {
            FormatType.IMAGE -> ImageUtils.generateThumbnail(file)
            FormatType.VIDEO -> VideoUtils.generateVideoThumbnail(file)
            else -> {
                Log.e(TAG, "/thumbnail/$id → Tipo no soportado: ${media.type}")
                return json(ApiResponse<Unit>(status = "ERROR", error = "unsupported"))
            }
        }

        Log.d(TAG, "Enviando thumbnail (${thumb.length()} bytes)")
        return newFixedLengthResponse(
            Response.Status.OK,
            "image/jpeg",
            thumb.inputStream(),
            thumb.length()
        )
    }

    // -------------------------------------------------------------------------
    // VINCULACIÓN: /show_pin y /pair
    // -------------------------------------------------------------------------

    /**
     * `POST /show_pin`
     *
     * Inicia el proceso de emparejamiento:
     * - Genera un PIN de 6 dígitos.
     * - Genera un SALT de 16 bytes.
     * - Guarda ambos en memoria (no en disco).
     * - Devuelve el SALT al móvil en Base64.
     *
     * El PIN se muestra en la tablet desde la capa de presentación; no viaja
     * por la red.
     */
    fun handleShowPin(): Response {
        Log.d(TAG, "/show_pin → Generando PIN y SALT para emparejamiento")

        // 1. Generar PIN
        val pin = PinGenerator.generatePin6()
        currentPin = pin

        _pinFlow.tryEmit(pin)

        // Aquí la capa de presentación debería ser notificada para mostrar el PIN.
        Log.d(TAG, "/show_pin → PIN generado (solo para UI de tablet) $pin")

        // 2. Generar SALT
        val salt = SaltGenerator.generateSalt16()
        currentSalt = salt

        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)

        val payload = mapOf(
            "status" to "pending",
            "salt" to saltBase64
        )

        Log.d(TAG, "/show_pin → SALT enviado al móvil")

        return json(ApiResponse(status = "OK", data = payload))
    }

    /**
     * `POST /pair`
     *
     * Completa el proceso de emparejamiento:
     * - Recibe el PIN introducido en el móvil.
     * - Comprueba que coincide con el PIN actual.
     * - Deriva una clave temporal con PBKDF2 usando PIN + SALT.
     * - Genera una clave AES REAL aleatoria (AES‑128).
     * - Cifra la clave AES REAL con la clave temporal.
     * - Guarda la clave AES REAL en preferencias.
     * - Devuelve al móvil:
     *   - SALT (por si lo necesita).
     *   - AES_REAL cifrada en Base64.
     */
    fun handlePair(session: IHTTPSession): Response {
        Log.d(TAG, "/pair → Petición de emparejamiento recibida")

        val files = HashMap<String, String>()
        session.parseBody(files)
        val rawPin = session.parameters["pin"]?.toString()
        val pinFromClient = rawPin?.replace("[", "")?.replace("]", "")

        if (pinFromClient.isNullOrBlank()) {
            Log.e(TAG, "/pair → PIN no proporcionado")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "pin missing"),
                Response.Status.BAD_REQUEST
            )
        }

        val expectedPin = currentPin
        val salt = currentSalt

        if (expectedPin == null || salt == null) {
            Log.e(TAG, "/pair → No hay PIN/SALT activos. Llamar antes a /show_pin.")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "pair not initialized"),
                Response.Status.BAD_REQUEST
            )
        }

        if (pinFromClient != expectedPin) {
            Log.e(TAG, "/pair → PIN incorrecto. Esperado=$expectedPin, recibido=$pinFromClient")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "invalid pin"),
                Response.Status.UNAUTHORIZED
            )
        }

        Log.d(TAG, "/pair → PIN válido. Derivando clave temporal PBKDF2.")

        // 1. Derivar clave temporal
        val derivedKey = Pbkdf2KeyDeriver.deriveKeyFromPin(pinFromClient, salt)
        if (derivedKey.isEmpty()) {
            Log.e(TAG, "/pair → Error derivando clave PBKDF2")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "pbkdf2 failed"),
                Response.Status.INTERNAL_ERROR
            )
        }

        // 2. Generar AES_REAL aleatoria (16 bytes → AES‑128)
        val aesReal = ByteArray(16)
        SecureRandom().nextBytes(aesReal)

        // 3. Cifrar AES_REAL con la clave derivada
        val cryptoForKey = Crypto(derivedKey)
        val encryptedAesKey = cryptoForKey.encrypt(aesReal)
        if (encryptedAesKey.isEmpty()) {
            Log.e(TAG, "/pair → Error cifrando AES_REAL")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "encrypt aes failed"),
                Response.Status.INTERNAL_ERROR
            )
        }

        // 4. Guardar AES_REAL en preferencias
        runBlocking {
            prefsRepository.setAesKey(aesReal)
            prefsRepository.setMobileConnected(true)
        }
        Log.d(TAG, "/pair → AES_REAL guardada en preferencias. Vinculación completada.")

        // 5. Preparar respuesta
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val encryptedAesBase64 = Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP)

        val payload = mapOf(
            "status" to "linked",
            "salt" to saltBase64,
            "encryptedAesKey" to encryptedAesBase64
        )

        // 6. Limpiar PIN/SALT en memoria
        currentPin = null
        currentSalt = null

        return json(ApiResponse(status = "OK", data = payload))
    }

    // -------------------------------------------------------------------------
    // /upload CIFRADO CON AES_REAL
    // -------------------------------------------------------------------------

    /**
     * `POST /upload`
     *
     * Recibe un archivo cifrado con AES/GCM usando la clave AES REAL compartida
     * tras la vinculación.
     *
     * Flujo:
     * 1. Cargar la clave AES REAL desde [PreferencesRepository].
     * 2. Si no existe, devolver error de no vinculado.
     * 3. Parsear el multipart y obtener el fichero temporal.
     * 4. Leer los bytes cifrados.
     * 5. Descifrar con `Crypto(aesKey).decrypt`.
     * 6. Guardar un fichero temporal con los bytes descifrados.
     * 7. Delegar en [ImportMediaFromServerUseCase].
     * 8. Responder con `ApiResponse<UploadResult>`.
     */
    private fun handleEncryptedUpload(session: IHTTPSession): Response {
        Log.d(TAG, "/upload → Petición de subida recibida")

        // 1. Cargar clave AES REAL
        val aesKey = runBlocking { prefsRepository.getAesKey().first() }
        if (aesKey == null) {
            Log.e(TAG, "/upload → No hay clave AES_REAL configurada. Dispositivo no vinculado.")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "not linked"),
                Response.Status.UNAUTHORIZED
            )
        }

        val crypto = Crypto(aesKey)

        // 2. Parse multipart body
        val files = HashMap<String, String>()
        session.parseBody(files)

        val tempPath = files["file"]
        if (tempPath == null) {
            Log.e(TAG, "/upload → Archivo no recibido en multipart")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "file missing"),
                Response.Status.BAD_REQUEST
            )
        }

        val tempFile = File(tempPath)
        val encryptedBytes = try {
            tempFile.readBytes()
        } catch (e: Exception) {
            Log.e(TAG, "/upload → Error leyendo fichero temporal: ${e.message}", e)
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "file read error"),
                Response.Status.INTERNAL_ERROR
            )
        }
        Log.d(TAG, "/upload → Bytes cifrados recibidos (${encryptedBytes.size} bytes)")

        // 3. Descifrado AES/GCM
        val decryptedBytes = crypto.decrypt(encryptedBytes)
        if (decryptedBytes.isEmpty()) {
            Log.e(TAG, "/upload → Error descifrando datos (clave incorrecta o datos corruptos)")
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "decrypt failed"),
                Response.Status.BAD_REQUEST
            )
        }

        // 4. Guardar fichero temporal descifrado
        val plainTempFile = File(tempFile.parentFile, "dec_${tempFile.name}")
        try {
            plainTempFile.writeBytes(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "/upload → Error escribiendo fichero descifrado: ${e.message}", e)
            return json(
                ApiResponse<Unit>(status = "ERROR", error = "file write error"),
                Response.Status.INTERNAL_ERROR
            )
        }
        Log.d(TAG, "/upload → Archivo descifrado guardado en ${plainTempFile.absolutePath}")

        // 5. Extraer metadata y delegar en use case
        val mime = session.headers["content-type"] ?: "application/octet-stream"
        val originalName = session.parameters["file"]?.firstOrNull() ?: "uploaded_file"
        val ext = originalName.substringAfterLast('.', "")

        Log.d(TAG, "/upload → Archivo descifrado: $originalName ($mime) ext=$ext")

        runBlocking {
            importMediaFromServerUseCase(plainTempFile, mime, ext)
        }

        Log.d(TAG, "/upload → Archivo importado correctamente en Room")

        return json(
            ApiResponse(
                status = "OK",
                data = UploadResult(
                    success = true,
                    uploaded = true
                )
            )
        )
    }

    // -------------------------------------------------------------------------
    // UTILIDADES
    // -------------------------------------------------------------------------

    /**
     * Serializa un [ApiResponse] a JSON usando Gson y devuelve una respuesta HTTP.
     *
     * @param body Cuerpo de la respuesta envuelto en `ApiResponse`.
     * @param status Código HTTP a devolver (por defecto 200 OK).
     */
    private fun json(body: ApiResponse<*>, status: Response.Status = Response.Status.OK): Response {
        val json = gson.toJson(body)
        Log.d(
            TAG,
            "Respondiendo JSON con estado=${body.status}, tamaño aproximado=${json.length} caracteres"
        )
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

    fun implementForQRScanner(): ApiResponse<QrPayload> {
        val pin = PinGenerator.generatePin6()
        currentPin = pin

        val salt = SaltGenerator.generateSalt16()
        currentSalt = salt

        val ip = NetworkUtils.getLocalIpAddress() ?: "0.0.0.0"

        val id = runBlocking { prefsRepository.getTabletId().first() }
        val nombre = runBlocking { prefsRepository.getTabletName().first() }

        val puerto = 8080

        val payload = QrPayload(
            pin = pin,
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            id = id,
            nombre = nombre,
            ip = ip,
            puerto = puerto,
            aesKey = null
        )

        return ApiResponse(status = "OK", data = payload)
    }
}
