package com.ajpr00.mobile.data.datasource.remote

import com.ajpr00.core.domain.model.api.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface TabletApi {
    @GET
    suspend fun ping(@Url url: String): String

    @GET
    suspend fun getInfo(@Url url: String): ApiResponse<DeviceInfo>

    @GET
    suspend fun listMedia(@Url url: String): ApiResponse<ListMediaData>

    @DELETE
    suspend fun deleteMedia(
        @Url url: String
    ): ApiResponse<DeleteResult>

    @GET
    suspend fun getMediaFile(@Url url: String): ResponseBody

    @GET
    suspend fun getThumbnail(@Url url: String): ResponseBody

    @Multipart
    @POST
    suspend fun uploadMedia(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResult>
}
