package com.ajpr00.mobile.data.repositoryImp

import android.util.Log
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.api.*
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.core.domain.repository.preference.SessionManager
import com.ajpr00.data.mapper.media.toDomain
import com.ajpr00.mobile.data.datasource.remote.TabletApi
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

/**
 * ## TabletApiRepositoryImpl
 *
 * Repositorio **remoto** encargado de comunicar la app móvil con el servidor HTTP
 * que corre dentro de la tablet (NanoHTTPD).
 *
 * ### Rol dentro de la arquitectura
 * - **Data (remote)**: encapsula Retrofit y construye URLs dinámicas.
 * - **Domain**: expone modelos limpios (`RemoteMedia`) sin filtrar detalles HTTP.
 * - **Presentation**: los ViewModels consumen este repo sin tocar infraestructura.
 *
 * ### Qué resuelve este repositorio
 * - Llamadas HTTP a la tablet (ping, info, media, thumbnails, upload…).
 * - Gestión del token AES para `/upload`.
 * - Combinación de JSON + binarios en `listMediaWithThumbnails()`.
 *
 * ### Notas clave
 * - Todas las respuestas siguen `ApiResponse<T>`.
 * - Si `status != "OK"`, se devuelven listas vacías para evitar errores en UI.
 * - Solo `uploadMedia()` captura excepciones para evitar crasheos.
 */
class TabletApiRepositoryImpl @Inject constructor(
    private val api: TabletApi,
    private val prefs: SessionManager
) : TabletApiRepository {

    /**
     * Construye la URL completa para un endpoint remoto.
     *
     * @param ip IP de la tablet.
     * @param port Puerto HTTP.
     * @param path Ruta del endpoint.
     *
     * @return URL completa lista para Retrofit.
     */
    private fun url(ip: String, port: Int, path: String): String {
        val full = "http://$ip:$port$path"
        Log.d("TabletApiRepo", "URL generada: $full")
        return full
    }

    /**
     * Comprueba si la tablet está activa llamando a `/ping`.
     */
    override suspend fun ping(ip: String, port: Int): ApiResponse<String> {
        Log.d("TabletApiRepo", "→ /ping")
        return api.ping(url(ip, port, "/ping"))
    }

    /**
     * Obtiene información básica del dispositivo desde `/info`.
     */
    override suspend fun getInfo(ip: String, port: Int): ApiResponse<DeviceInfo>? {
        Log.d("TabletApiRepo", "→ /info")
        return api.getInfo(url(ip, port, "/info"))
    }

    /**
     * Recupera la lista de archivos remotos desde `/list_media`.
     */
    override suspend fun listMedia(ip: String, port: Int): ApiResponse<ListMediaData>? {
        Log.d("TabletApiRepo", "→ /list_media")
        return api.listMedia(url(ip, port, "/list_media"))
    }

    /**
     * Elimina un archivo remoto llamando a `/delete/{id}`.
     */
    override suspend fun deleteMedia(
        ip: String,
        port: Int,
        id: String
    ): ApiResponse<DeleteResult>? {
        Log.d("TabletApiRepo", "→ /delete/$id")
        return api.deleteMedia(url(ip, port, "/delete/$id"))
    }

    /**
     * Descarga un archivo remoto desde `/media/{id}`.
     */
    override suspend fun getMediaFile(ip: String, port: Int, id: String): ResponseBody {
        Log.d("TabletApiRepo", "Descargando archivo id=$id")
        return api.getMediaFile(url(ip, port, "/media/$id"))
    }

    /**
     * Descarga un thumbnail JPEG desde `/thumbnail/{id}`.
     */
    override suspend fun getThumbnail(ip: String, port: Int, id: String): ResponseBody {
        Log.d("TabletApiRepo", "Descargando thumbnail id=$id")
        return api.getThumbnail(url(ip, port, "/thumbnail/$id"))
    }

    /**
     * Sube un archivo cifrado al servidor usando `/upload`.
     *
     * ### Flujo interno
     * 1. Obtiene el token AES desde Preferences.
     * 2. Construye la URL del endpoint.
     * 3. Envía el multipart con el archivo cifrado.
     * 4. Devuelve `ApiResponse<UploadResult>` o `null` si falla.
     *
     * ### Advertencias
     * - Si el token está vacío, el servidor rechazará la subida.
     * - Las excepciones se silencian devolviendo `null` para evitar crasheos.
     */
    override suspend fun uploadMedia(
        ip: String,
        port: Int,
        file: MultipartBody.Part,
        originalName: RequestBody,
        originalExt: RequestBody,
        originalMime: RequestBody
    ): ApiResponse<UploadResult> {

        val token = prefs.getAesToken().first()
        val url = "http://$ip:$port/upload"

        return try {
            api.uploadMedia(
                url,
                token,
                file,
                originalName,
                originalExt,
                originalMime
            )
        } catch (e: Exception) {
            Log.e("TabletApiRepo", "Error en uploadMedia: ${e.message}")
            ApiResponse(status = "ERROR", error = "upload error")
        }
    }



    /**
     * Combina `/list_media` + `/thumbnail/{id}` para obtener una lista completa
     * de `RemoteMedia` lista para UI.
     *
     * ### Flujo interno
     * 1. Llama a `listMedia()`.
     * 2. Si `status != OK`, devuelve lista vacía.
     * 3. Para cada media:
     *    - descarga thumbnail
     *    - convierte a dominio con `toDomain()`
     */
    override suspend fun listMediaWithThumbnails(ip: String, port: Int): List<RemoteMedia> {
        Log.d("TabletApiRepo", "Obteniendo media + thumbnails")

        val response = listMedia(ip, port) ?: run {
            Log.w("TabletApiRepo", "listMedia devolvió null")
            return emptyList()
        }

        if (response.status != "OK") {
            Log.w("TabletApiRepo", "Estado no OK: ${response.status}")
            return emptyList()
        }

        return response.data?.items?.map { media ->
            Log.d("TabletApiRepo", "Thumbnail → id=${media.id}")
            val thumbBytes = getThumbnail(ip, port, media.id).bytes()
            media.toDomain(thumbBytes)
        } ?: emptyList()
    }

    /**
     * Solicita a la tablet que muestre el PIN en pantalla.
     */
    override suspend fun showPin(ip: String, port: Int): ApiResponse<Map<String, String>>? {
        Log.d("TabletApiRepo", "→ /show_pin")
        return api.showPin(url(ip, port, "/show_pin"))
    }

    /**
     * Envía el PIN introducido por el usuario para emparejar móvil ↔ tablet.
     */
    override suspend fun pair(
        ip: String,
        port: Int,
        pin: String
    ): ApiResponse<Map<String, String>>? {
        Log.d("TabletApiRepo", "→ /pair (PIN enviado)")
        return api.pair(url(ip, port, "/pair"), pin)
    }
}