package com.ajpr00.data.datasource.cloud

import android.util.Log
import com.ajpr00.core.domain.model.GoogleConfig
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.data.util.detectFormatType
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class DataSourceGoogleDrive @Inject constructor(
    private val config: GoogleConfig
) {
    private val clientId: String = config.clientId
    private val clientSecret: String = config.clientSecret
    private val redirectUri: String = config.redirectUri

    private val http = OkHttpClient()

    // Intercambiar SERVER AUTH CODE → ACCESS TOKEN
    // Básicamente: el "serverAuthCode" que nos da Google Sign-In es como un ticket de entrada.
    // Aquí lo cambiamos por un "access_token", que es la llave real para abrir la puerta de la API de Drive.
    private suspend fun exchangeAuthCode(authCode: String): String? =
        withContext(Dispatchers.IO) {
            Log.d("GoogleDriveRepo", "Intercambiando serverAuthCode por accessToken...")
            Log.d("GoogleDriveRepo", "🔍 Enviando a token endpoint con:")
            Log.d("GoogleDriveRepo", "   • client_id=$clientId")
            Log.d("GoogleDriveRepo", "   • client_secret=$clientSecret")
            Log.d("GoogleDriveRepo", "   • code=$authCode")
            Log.d("GoogleDriveRepo", "   • redirect_uri=$redirectUri")

            val body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", authCode)
                .add("redirect_uri", redirectUri)
                .build()

            val request = Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(body)
                .build()

            val response = http.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("GoogleDriveRepo", "Error al pedir accessToken: ${response.code}")
                return@withContext null
            }

            val json = JSONObject(response.body!!.string())
            val accessToken = json.getString("access_token")

            Log.d("GoogleDriveRepo", "AccessToken recibido: $accessToken")
            accessToken
        }

    // 2️⃣ Listar archivos con el ACCESS TOKEN
    // Ahora que tenemos la llave (accessToken), podemos pedirle a Drive que nos muestre los archivos.
    // Aquí hacemos una llamada REST a la API de Drive y pedimos id, nombre y tipo de archivo.
    suspend fun listDriveFiles(accessToken: String): List<MediaContent> =
        withContext(Dispatchers.IO) {
            Log.d("GoogleDriveRepo", "Listando imágenes y vídeos de Drive...")

            val request = Request.Builder()
                .url(
                    "https://www.googleapis.com/drive/v3/files?" +
                            "q=mimeType contains 'image/' or mimeType contains 'video/'" +
                            "&fields=files(id,name,mimeType,thumbnailLink)"
                )
                .header("Authorization", "Bearer $accessToken")
                .build()

            val response = http.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("GoogleDriveRepo", "Error al listar archivos multimedia: ${response.code}")
                return@withContext emptyList<MediaContent>()
            }

            val json = JSONObject(response.body!!.string())
            val files = json.getJSONArray("files")

            Log.d("GoogleDriveRepo", "Número de archivos multimedia recibidos: ${files.length()}")

            // Transformamos y filtramos en un solo paso
            val mediaList = List(files.length()) { i ->
                val obj = files.getJSONObject(i)
                val name = obj.optString("name", "")
                val mime = obj.optString("mimeType", "")
                val thumb = obj.optString("thumbnailLink", "")
                val type = detectFormatType(mime = mime)

                MediaContent(
                    id = name.hashCode().toString(),
                    name = name,
                    path = thumb,
                    type = type,
                )
            }

            mediaList
        }

}
