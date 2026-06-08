package com.ajpr00.core.domain.repository.media

import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.api.ApiResponse
import com.ajpr00.core.domain.model.api.DeleteResult
import com.ajpr00.core.domain.model.api.DeviceInfo
import com.ajpr00.core.domain.model.api.ListMediaData
import com.ajpr00.core.domain.model.api.UploadResult
import okhttp3.MultipartBody
import okhttp3.ResponseBody

/**
 * TabletApiRepository
 *
 * Interfaz del dominio encargada de comunicar el móvil con la tablet a través
 * del servidor HTTP local. Define todas las operaciones disponibles en la API
 * de la tablet:
 *
 *  - Comprobar conexión (ping)
 *  - Obtener información del dispositivo
 *  - Listar archivos multimedia almacenados en la tablet
 *  - Eliminar un media por ID
 *  - Descargar un archivo o su miniatura
 *  - Subir un archivo mediante multipart
 *
 * La implementación real usa Retrofit y se encuentra en el módulo "data".
 */

interface TabletApiRepository {

    suspend fun ping(ip: String, port: Int): String?

    suspend fun getInfo(ip: String, port: Int): ApiResponse<DeviceInfo>?

    suspend fun listMedia(ip: String, port: Int): ApiResponse<ListMediaData>?

    suspend fun deleteMedia(ip: String, port: Int, id: String): ApiResponse<DeleteResult>?

    suspend fun getMediaFile(ip: String, port: Int, id: String): ResponseBody?

    suspend fun getThumbnail(ip: String, port: Int, id: String): ResponseBody?

    suspend fun uploadMedia(ip: String, port: Int, file: MultipartBody.Part): ApiResponse<UploadResult>?

    suspend fun listMediaWithThumbnails(ip: String, port: Int): List<RemoteMedia>

    suspend fun showPin(ip: String, port: Int): ApiResponse<Map<String, String>>?

    suspend fun pair(ip: String, port: Int, pin: String): ApiResponse<Map<String, String>>?
}

