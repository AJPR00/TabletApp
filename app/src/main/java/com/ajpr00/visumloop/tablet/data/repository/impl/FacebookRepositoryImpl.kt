package com.ajpr00.visumloop.tablet.data.repository.impl

import com.ajpr00.visumloop.tablet.data.datasource.cloud.FirebaseAuthDataSource
import com.ajpr00.visumloop.tablet.data.repository.FacebookRepository
import jakarta.inject.Inject

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
