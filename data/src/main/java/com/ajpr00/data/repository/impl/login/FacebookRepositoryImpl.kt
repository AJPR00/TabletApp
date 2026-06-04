package com.ajpr00.data.repository.impl.login

import com.ajpr00.core.domain.repository.login.FacebookAuthRepository
import com.ajpr00.data.datasource.login.FirebaseAuthDataSource
import javax.inject.Inject

class FacebookRepositoryImpl @Inject constructor(
    private val firebaseDS: FirebaseAuthDataSource
) : FacebookAuthRepository {

    override suspend fun loginWithFacebook(token: String): Result<Unit> {
        return try {
            firebaseDS.loginWithFacebook(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
