package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.api.*
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.data.mapper.media.toDomain
import com.ajpr00.mobile.data.datasource.remote.TabletApi
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

/**
 * # TabletApiRepositoryImpl
 *
 * Repositorio **remoto** encargado de comunicar la app móvil con el servidor HTTP
 * que corre dentro de la tablet (NanoHTTPD).
 *
 * ## ¿Qué hace este repositorio?
 * - Construye las URLs completas (`http://ip:port/ruta`).
 * - Llama a los endpoints definidos en [TabletApi] (Retrofit).
 * - Devuelve modelos del dominio (`RemoteMedia`) cuando es necesario.
 * - Gestiona el token de autenticación para `/upload`.
 *
 * ## Rol dentro de la arquitectura
 * - **Data layer (remote)**: puente entre Retrofit y los UseCases.
 * - **Domain layer**: los UseCases dependen de esta interfaz para no acoplarse a Retrofit.
 * - **Presentation layer**: nunca toca Retrofit directamente; siempre pasa por este repo.
 *
 * ## Notas importantes
 * - Todas las respuestas JSON siguen el wrapper `ApiResponse<T>`.
 * - El servidor devuelve `UploadResult(success, uploaded)` y Gson lo parsea sin problemas.
 * - `listMediaWithThumbnails()` combina datos JSON + bytes binarios (thumbnails).
 *
 * ## Advertencias
 * - Si el servidor devuelve `status != "OK"`, este repo devuelve listas vacías.
 * - No se capturan excepciones aquí; se delega a los UseCases o ViewModels.
 */
class TabletApiRepositoryImpl @Inject constructor(
    private val api: TabletApi
) : TabletApiRepository {

    /**
     * Construye la URL completa para cada endpoint.
     *
     * @param ip Dirección IP de la tablet descubierta por mDNS.
     * @param port Puerto HTTP (normalmente 8080).
     * @param path Ruta del endpoint (ej: "/ping").
     *
     * @return URL completa lista para Retrofit.
     *
     * ### Ejemplo
     * ```kotlin
     * url("192.168.1.33", 8080, "/ping")
     * // → "http://192.168.1.33:8080/ping"
     * ```
     */
    private fun url(ip: String, port: Int, path: String): String {
        return "http://$ip:$port$path"
    }

    /**
     * Llama al endpoint `/ping` para comprobar si la tablet está viva.
     *
     * @return `"pong"` si el servidor responde correctamente.
     */
    override suspend fun ping(ip: String, port: Int): String {
        return api.ping(url(ip, port, "/ping"))
    }

    /**
     * Llama al endpoint `/info` para obtener información básica del dispositivo.
     *
     * @return ApiResponse<DeviceInfo> o null si Retrofit falla.
     */
    override suspend fun getInfo(ip: String, port: Int): ApiResponse<DeviceInfo>? {
        return api.getInfo(url(ip, port, "/info"))
    }

    /**
     * Llama al endpoint `/list_media` para obtener la lista de media remota.
     *
     * @return ApiResponse<ListMediaData> con la lista de archivos.
     */
    override suspend fun listMedia(ip: String, port: Int): ApiResponse<ListMediaData>? {
        return api.listMedia(url(ip, port, "/list_media"))
    }

    /**
     * Llama al endpoint `/delete/{id}` para eliminar un archivo remoto.
     *
     * @param id ID del archivo a eliminar.
     * @return ApiResponse<DeleteResult> con el ID eliminado.
     */
    override suspend fun deleteMedia(ip: String, port: Int, id: String): ApiResponse<DeleteResult>? {
        return api.deleteMedia(url(ip, port, "/delete/$id"))
    }

    /**
     * Descarga un archivo remoto desde `/media/{id}`.
     *
     * @return [ResponseBody] con el stream binario del archivo.
     */
    override suspend fun getMediaFile(ip: String, port: Int, id: String): ResponseBody {
        return api.getMediaFile(url(ip, port, "/media/$id"))
    }

    /**
     * Descarga un thumbnail desde `/thumbnail/{id}`.
     *
     * @return [ResponseBody] con los bytes JPEG del thumbnail.
     */
    override suspend fun getThumbnail(ip: String, port: Int, id: String): ResponseBody {
        return api.getThumbnail(url(ip, port, "/thumbnail/$id"))
    }

    /**
     * Sube un archivo cifrado al servidor usando `/upload`.
     *
     * Flujo:
     * 1. Construye la URL.
     * 2. Añade el token de autenticación.
     * 3. Envía el multipart con el archivo cifrado.
     * 4. Recibe `ApiResponse<UploadResult>`.
     *
     * @param file Archivo cifrado en un MultipartBody.Part.
     * @return ApiResponse<UploadResult> con `success` y `uploaded`.
     */
    override suspend fun uploadMedia(
        ip: String,
        port: Int,
        file: MultipartBody.Part
    ): ApiResponse<UploadResult>? {
        val token = "TOKEN_SECRETO_COMPARTIDO"
        return api.uploadMedia(url(ip, port, "/upload"), token, file)
    }

    /**
     * Combina `/list_media` + `/thumbnail/{id}` para obtener:
     * - metadatos del archivo remoto
     * - thumbnail en bytes
     *
     * @return Lista de [RemoteMedia] lista para UI.
     *
     * ### Flujo interno
     * 1. Llama a `listMedia()`.
     * 2. Si `status != OK`, devuelve lista vacía.
     * 3. Para cada media:
     *    - descarga thumbnail
     *    - convierte a modelo de dominio con `toDomain()`
     */
    override suspend fun listMediaWithThumbnails(ip: String, port: Int): List<RemoteMedia> {
        val response = listMedia(ip, port) ?: return emptyList()
        if (response.status != "OK") return emptyList()

        return response.data?.items?.map { media ->
            val thumbBytes = getThumbnail(ip, port, media.id).bytes()
            media.toDomain(thumbBytes)
        } ?: emptyList()
    }
}
