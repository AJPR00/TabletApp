package com.ajpr00.data.repository.impl

import com.ajpr00.core.domain.model.UserGoogle
import com.ajpr00.core.domain.repository.GoogleAuthRepository
import com.ajpr00.data.datasource.cloud.FirebaseAuthDataSource
import com.ajpr00.data.datasource.cloud.GoogleCredentialDataSource
import com.ajpr00.data.datasource.local.preferences.SessionPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    private val credentialDS: GoogleCredentialDataSource,
    private val firebaseDS: FirebaseAuthDataSource,
    private val sessionDS: SessionPreference
): GoogleAuthRepository {

    override suspend fun loginWithGoogle(): Result<Unit> {
        return try {
            val credential = credentialDS.getGoogleSignInCredential()
            val googleCred = credentialDS.extractGoogleIdToken(credential)
            val idToken = googleCred.idToken

            firebaseDS.loginWithIdToken(idToken) // si falla → excepción

            sessionDS.saveLocalSession(
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
        sessionDS.clearLocalSession()
        return Result.success(Unit)
    }

    override suspend fun refreshSession(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getLocalUser(): Flow<UserGoogle?> {
        TODO("Not yet implemented")
    }

    override val isLocalLoggedIn: Flow<Boolean> = sessionDS.idTokenLocal
        .map { !it.isNullOrEmpty()
        }

}

