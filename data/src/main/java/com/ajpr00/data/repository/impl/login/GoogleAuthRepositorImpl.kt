package com.ajpr00.data.repository.impl.login

import com.ajpr00.core.domain.model.UserGoogle
import com.ajpr00.core.domain.repository.login.AuthPreference
import com.ajpr00.core.domain.repository.login.GoogleAuthRepository
import com.ajpr00.data.datasource.login.FirebaseAuthDataSource
import com.ajpr00.data.datasource.cloud.GoogleCredentialDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    private val credentialDS: GoogleCredentialDataSource,
    private val firebaseDS: FirebaseAuthDataSource,
    private val authPreference: AuthPreference,
): GoogleAuthRepository {

    override suspend fun loginWithGoogle(): Result<Unit> {
        return try {
            val credential = credentialDS.getGoogleSignInCredential()
            val googleCred = credentialDS.extractGoogleIdToken(credential)
            val idToken = googleCred.idToken

            firebaseDS.loginWithIdToken(idToken) // si falla → excepción

            authPreference.saveLocalSession(
                token = idToken,
                email = googleCred.id,
                username = googleCred.displayName ?: "",
                avatarUrl = googleCred.profilePictureUri?.toString().orEmpty(),
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        firebaseDS.logout()
        authPreference.clearAll()
        return Result.success(Unit)
    }

    override suspend fun refreshSession(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getLocalUser(): Flow<UserGoogle?> {
        TODO("Not yet implemented")
    }

    override val isLocalLoggedIn: Flow<Boolean> = authPreference.idTokenLocal
        .map { !it.isNullOrEmpty()
        }

}

