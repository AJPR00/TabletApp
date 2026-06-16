package com.ajpr00.data.repository.impl.network

import com.ajpr00.core.domain.repository.network.NetworkRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

/**
 * Implementación del repositorio de red que comprueba si existe
 * conexión real a Internet realizando una petición a un endpoint
 * que devuelve HTTP 204 sin contenido.
 */
class NetworkRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : NetworkRepository {

    companion object {
        private const val TEST_URL = "https://clients3.google.com/generate_204"
    }

    override suspend fun hasInternetConnection(): Boolean {
        return try {
            val request = Request.Builder()
                .url(TEST_URL)
                .build()

            client.newCall(request).execute().use { response ->
                response.code == 204
            }
        } catch (e: Exception) {
            false
        }
    }
}