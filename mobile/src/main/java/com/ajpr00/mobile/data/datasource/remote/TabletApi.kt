package com.ajpr00.mobile.data.datasource.remote

import com.ajpr00.core.domain.model.api.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * # TabletApi
 *
 * Interfaz Retrofit que define **todas las llamadas HTTP** que la app móvil
 * realiza contra el servidor NanoHTTPD que corre dentro de la tablet.
 *
 * ## ¿Qué hace esta interfaz?
 * - Define los endpoints REST expuestos por la tablet.
 * - Gestiona rutas dinámicas usando `@Url`.
 * - Gestiona subida de archivos cifrados mediante multipart.
 * - Devuelve modelos envueltos en `ApiResponse<T>` para compatibilidad con Gson.
 *
 * ## Rol dentro de la arquitectura
 * - **Infraestructura (HTTP)**: solo describe endpoints.
 * - **Data layer**: es consumida por `TabletApiRepositoryImpl`.
 * - **Domain layer**: nunca toca Retrofit directamente; usa el repositorio.
 * - **Presentation layer**: nunca ve esta interfaz.
 *
 * ## Notas importantes
 * - Todas las rutas usan `@Url` porque la IP y el puerto son dinámicos (mDNS).
 * - La subida de archivos requiere un header `X-Auth-Token`.
 * - Los endpoints que devuelven binarios usan `ResponseBody`.
 *
 * ## Advertencias
 * - No hacer lógica aquí: solo definición de endpoints.
 * - No capturar errores aquí: Retrofit los lanza hacia el repositorio.
 */
interface TabletApi {

    /**
     * ## `GET /ping`
     *
     * Comprueba si la tablet está viva.
     *
     * @param url URL completa generada por el repositorio.
     * @return `"pong"` si el servidor responde correctamente.
     *
     * ### Ejemplo
     * ```kotlin
     * api.ping("http://192.168.1.33:8080/ping")
     * ```
     */
    @GET
    suspend fun ping(@Url url: String): String

    /**
     * ## `GET /info`
     *
     * Devuelve información básica del dispositivo:
     * - id
     * - nombre
     * - puerto
     *
     * @param url URL completa del endpoint.
     * @return ApiResponse<DeviceInfo> parseado por Gson.
     */
    @GET
    suspend fun getInfo(@Url url: String): ApiResponse<DeviceInfo>

    /**
     * ## `GET /list_media`
     *
     * Devuelve la lista de media almacenada en la tablet.
     *
     * @param url URL completa del endpoint.
     * @return ApiResponse<ListMediaData> con la lista de archivos.
     */
    @GET
    suspend fun listMedia(@Url url: String): ApiResponse<ListMediaData>

    /**
     * ## `DELETE /delete/{id}`
     *
     * Elimina un archivo remoto por ID.
     *
     * @param url URL completa con el ID incluido.
     * @return ApiResponse<DeleteResult> con el ID eliminado.
     */
    @DELETE
    suspend fun deleteMedia(@Url url: String): ApiResponse<DeleteResult>

    /**
     * ## `GET /media/{id}`
     *
     * Descarga un archivo original desde la tablet.
     *
     * @param url URL completa del archivo.
     * @return [ResponseBody] con el stream binario.
     *
     * ### Nota
     * - No se envuelve en ApiResponse porque es un binario puro.
     */
    @GET
    suspend fun getMediaFile(@Url url: String): ResponseBody

    /**
     * ## `GET /thumbnail/{id}`
     *
     * Descarga un thumbnail JPEG generado por la tablet.
     *
     * @param url URL completa del thumbnail.
     * @return [ResponseBody] con los bytes JPEG.
     */
    @GET
    suspend fun getThumbnail(@Url url: String): ResponseBody

    /**
     * ## `POST /upload`
     *
     * Sube un archivo **cifrado con AES/GCM** al servidor NanoHTTPD.
     *
     * Flujo:
     * 1. El repositorio construye la URL dinámica.
     * 2. Añade el header `X-Auth-Token`.
     * 3. Envía el archivo en un `MultipartBody.Part`.
     * 4. El servidor descifra, guarda y responde con `UploadResult`.
     *
     * @param url URL completa del endpoint.
     * @param token Token de autenticación compartido.
     * @param file Archivo cifrado en multipart.
     *
     * @return ApiResponse<UploadResult> con:
     * - `success = true`
     * - `uploaded = true`
     *
     * ### Advertencia
     * - Si el token es incorrecto → 401.
     * - Si el archivo no se descifra → 400.
     */
    @Multipart
    @POST
    suspend fun uploadMedia(
        @Url url: String,
        @Header("X-Auth-Token") token: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResult>
}