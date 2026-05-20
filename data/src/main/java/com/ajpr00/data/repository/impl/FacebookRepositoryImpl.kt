package com.ajpr00.data.repository.impl

import com.ajpr00.core.domain.repository.FacebookRepository
import com.ajpr00.data.datasource.cloud.FirebaseAuthDataSource
import javax.inject.Inject

class FacebookRepositoryImpl @Inject constructor(
    private val firebaseDS: FirebaseAuthDataSource
) : FacebookRepository {

    override suspend fun loginWithFacebook(token: String): Result<Unit> {
        return try {
            firebaseDS.loginWithFacebook(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
