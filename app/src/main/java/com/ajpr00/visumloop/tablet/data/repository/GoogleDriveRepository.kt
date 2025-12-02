package com.ajpr00.visumloop.tablet.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class GoogleDriveRepository @Inject constructor(
    private val clientId: String,
    private val clientSecret: String    // Necesario para OAuth (Web client)
) {
    private val http = OkHttpClient()

    // 1️⃣ Intercambiar ID TOKEN → ACCESS TOKEN
    private suspend fun exchangeIdToken(idToken: String): String? =
        withContext(Dispatchers.IO) {

            val body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("id_token", idToken)
                .build()

            val request = Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(body)
                .build()

            val response = http.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val json = JSONObject(response.body!!.string())
            json.getString("access_token")
        }

    // 2️⃣ Listar archivos con el ACCESS TOKEN
    suspend fun listDriveFiles(idToken: String): List<String> =
        withContext(Dispatchers.IO) {

            val accessToken = exchangeIdToken(idToken) ?: return@withContext emptyList()

            val request = Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?fields=files(id,name,mimeType)")
                .header("Authorization", "Bearer $accessToken")
                .build()

            val response = http.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val json = JSONObject(response.body!!.string())
            val files = json.getJSONArray("files")

            List(files.length()) { i ->
                files.getJSONObject(i).getString("name")
            }
        }
}
