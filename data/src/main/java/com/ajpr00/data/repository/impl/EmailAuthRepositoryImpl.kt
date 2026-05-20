package com.ajpr00.data.repository.impl

import android.util.Log
import com.ajpr00.core.domain.repository.EmailAuthRepository
import com.ajpr00.data.datasource.cloud.FirebaseAuthDataSource
import com.ajpr00.data.datasource.local.preferences.SessionPreference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EmailAuthRepositoryImpl @Inject constructor(
    private val sessionDS: SessionPreference,
    private val firebaseDS: FirebaseAuthDataSource
): EmailAuthRepository {

    // Login con email y contraseña usando Firebase.
    // Devolvemos Result<Unit> para que el que llame pueda saber si fue bien o mal.
    override suspend fun loginEmail(email: String, password: String): Result<Unit> {
        Log.d("LoginRepo", "Intentando login con Firebase para: $email")
        return try {
            // Esperamos a que Firebase responda (sin callbacks, gracias a await()).
            firebaseDS.loginWithEmailAndPassword(email, password)
            Log.d("LoginRepo", "🟢 Firebase autenticó correctamente al usuario")

            val user = firebaseDS.getCurrentUser()
            val idToken = user?.getIdToken(true)?.await()?.token.orEmpty()
            Log.d("LoginRepo", "Token obtenido: ${idToken.take(10)}... (truncado)")
            Log.d("LoginRepo", "Avatar: ${user?.photoUrl}")

            // Guardamos la sesión localmente para no pedir login cada vez.
            sessionDS.saveLocalSession(
                username = email,
                email = email,
                token = idToken,
                avatarUrl = user?.photoUrl?.toString().orEmpty()
            )
            Log.d("LoginRepo", "💾 Sesión guardada en LoginPreferences")

            Result.success(Unit)
        } catch (e: Exception) {
            // Si Firebase falla, lo capturamos y devolvemos el error.
            Log.e("LoginRepo", "Error en login: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun registerEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseDS.registerEmail(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun userExists(email: String): Boolean {
        return firebaseDS.userExists(email)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseDS.sendPasswordReset(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String {
        return firebaseDS.getCurrentUserToken()
    }
}