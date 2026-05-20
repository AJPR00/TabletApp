package com.ajpr00.data.repository.impl


import com.ajpr00.core.domain.repository.DropboxRepository
import com.ajpr00.data.datasource.cloud.FirebaseAuthDataSource
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
