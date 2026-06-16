package com.ajpr00.data.repository.impl.network


import com.ajpr00.data.repository.network.DropboxRepository
import com.ajpr00.data.datasource.login.FirebaseAuthDataSource
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
