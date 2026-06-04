package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.api.*
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.data.mapper.media.toDomain
import com.ajpr00.mobile.data.datasource.remote.TabletApi
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class TabletApiRepositoryImpl @Inject constructor(
    private val api: TabletApi
) : TabletApiRepository {

    private fun url(ip: String, port: Int, path: String): String {
        return "http://$ip:$port$path"
    }

    override suspend fun ping(ip: String, port: Int): String {
        return api.ping(url(ip, port, "/ping"))
    }

    override suspend fun getInfo(ip: String, port: Int): ApiResponse<DeviceInfo>? {
        return api.getInfo(url(ip, port, "/info"))
    }

    override suspend fun listMedia(ip: String, port: Int): ApiResponse<ListMediaData>? {
        return api.listMedia(url(ip, port, "/list_media"))
    }

    override suspend fun deleteMedia(ip: String, port: Int, id: String): ApiResponse<DeleteResult>? {
        return api.deleteMedia(url(ip, port, "/delete/$id"))
    }

    override suspend fun getMediaFile(ip: String, port: Int, id: String): ResponseBody {
        return api.getMediaFile(url(ip, port, "/media/$id"))
    }

    override suspend fun getThumbnail(ip: String, port: Int, id: String): ResponseBody {
        return api.getThumbnail(url(ip, port, "/thumbnail/$id"))
    }

    override suspend fun uploadMedia(
        ip: String,
        port: Int,
        file: MultipartBody.Part
    ): ApiResponse<UploadResult>? {
        return api.uploadMedia(url(ip, port, "/upload"), file)
    }

    override suspend fun listMediaWithThumbnails(ip: String, port: Int): List<RemoteMedia> {
        val response = listMedia(ip, port) ?: return emptyList()
        if (response.status != "OK") return emptyList()

        return response.data?.items?.map { media ->
            val thumbBytes = getThumbnail(ip, port, media.id).bytes()
            media.toDomain(thumbBytes)
        } ?: emptyList()
    }
}
