package com.ajpr00.data.repository.impl.login

import android.util.Log
import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.repository.login.AuthRepository
import com.ajpr00.core.domain.repository.preference.SessionManager
import com.ajpr00.data.datasource.login.FirebaseAuthDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authPreference: SessionManager,
    private val firebaseDS: FirebaseAuthDataSource
) : AuthRepository {
    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
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
            authPreference.saveLocalSession(
                UserAuthData(
                    token = idToken,
                    email = email,
                    username = user?.displayName.orEmpty(),
                    avatarUrl = user?.photoUrl?.toString().orEmpty(),
                    authCode = null
                )
            )
            Log.d("LoginRepo", "💾 Sesión guardada en LoginPreferences")

            Result.success(Unit)
        } catch (e: Exception) {
            // Si Firebase falla, lo capturamos y devolvemos el error.
            Log.e("LoginRepo", "Error en login: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseDS.registerEmail(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<UserAuthData> {
        TODO("Not yet implemented")
    }

    override suspend fun loginWithFacebook(token: String): Result<UserAuthData> {
        return try {
            val firebaseUser = firebaseDS.loginWithFacebook(token)

            val user = UserAuthData(
                token = token,
                email = firebaseUser.email ?: "",
                username = firebaseUser.displayName ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                authCode = null, // Facebook no usa authCode
            )

            Result.success(user)

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

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseDS.logout()
            authPreference.clearLocalSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}