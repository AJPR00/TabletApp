package com.ajpr00.visumloop.tablet.data.repository.impl

import com.ajpr00.visumloop.tablet.data.datasource.cloud.FirebaseAuthDataSource
import com.ajpr00.visumloop.tablet.data.repository.DropboxRepository
import javax.inject.Inject

class DropboxRepositoryImpl @Inject constructor(
    private val firebaseDS: FirebaseAuthDataSource
) : DropboxRepository {

    override suspend fun loginWithDropbox(token: String): Result<Unit> {
        return try {
            firebaseDS.loginWithDropbox(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
