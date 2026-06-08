package com.ajpr00.mobile.data.datasource.remote

import com.ajpr00.core.domain.model.api.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * # TabletApi
 *
 * Interfaz Retrofit que define todas las llamadas HTTP que la app móvil
 * realiza contra el servidor NanoHTTPD que corre dentro de la tablet.
 *
 * Cada método usa @Url porque la IP y el puerto se descubren por mDNS.
 */
interface TabletApi {

    // -------------------------------------------------------------------------
    // BÁSICOS
    // -------------------------------------------------------------------------

    /**
     * ## GET /ping
     * Comprueba si la tablet está viva.
     */
    @GET
    suspend fun ping(@Url url: String): String

    /**
     * ## GET /info
     * Devuelve id, nombre y puerto de la tablet.
     */
    @GET
    suspend fun getInfo(@Url url: String): ApiResponse<DeviceInfo>

    /**
     * ## GET /list_media
     * Lista todos los archivos multimedia almacenados en la tablet.
     */
    @GET
    suspend fun listMedia(@Url url: String): ApiResponse<ListMediaData>

    /**
     * ## DELETE /delete/{id}
     * Elimina un archivo remoto por ID.
     */
    @DELETE
    suspend fun deleteMedia(@Url url: String): ApiResponse<DeleteResult>

    /**
     * ## GET /media/{id}
     * Descarga un archivo original.
     */
    @GET
    suspend fun getMediaFile(@Url url: String): ResponseBody

    /**
     * ## GET /thumbnail/{id}
     * Descarga un thumbnail JPEG generado por la tablet.
     */
    @GET
    suspend fun getThumbnail(@Url url: String): ResponseBody

    // -------------------------------------------------------------------------
    // PAIRING
    // -------------------------------------------------------------------------

    /**
     * ## POST /show_pin
     *
     * Inicia el proceso de emparejamiento.
     * Devuelve:
     * - status = "pending"
     * - salt = Base64
     *
     * El PIN **NO** se devuelve aquí. Solo se muestra en la tablet.
     */
    @POST
    suspend fun showPin(@Url url: String): ApiResponse<Map<String, String>>

    /**
     * ## POST /pair
     *
     * Completa el emparejamiento.
     * Envía el PIN introducido por el usuario.
     *
     * Devuelve:
     * - status = "linked"
     * - salt = Base64
     * - encryptedAesKey = Base64
     */
    @FormUrlEncoded
    @POST
    suspend fun pair(
        @Url url: String,
        @Field("pin") pin: String
    ): ApiResponse<Map<String, String>>

    // -------------------------------------------------------------------------
    // UPLOAD CIFRADO
    // -------------------------------------------------------------------------

    /**
     * ## POST /upload
     *
     * Sube un archivo cifrado con AES/GCM usando AES_REAL.
     */
    @Multipart
    @POST
    suspend fun uploadMedia(
        @Url url: String,
        @Header("X-Auth-Token") token: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResult>
}