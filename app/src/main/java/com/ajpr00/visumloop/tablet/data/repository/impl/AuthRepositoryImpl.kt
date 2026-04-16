package com.ajpr00.visumloop.tablet.data.repository.impl

import com.ajpr00.visumloop.tablet.data.datasource.cloud.FirebaseAuthDataSource
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.SessionPreference
import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import com.ajpr00.visumloop.tablet.domain.model.UserGoogle
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val sessionPreference: SessionPreference,
    private val firebaseDataSource: FirebaseAuthDataSource,
    ) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        sessionPreference.idTokenLocal.map { it != null }

    override val currentUser: Flow<UserGoogle?> =
        combine(
            sessionPreference.usernameLocal,
            sessionPreference.emailLocal,
            sessionPreference.avatarLocal,
            sessionPreference.idTokenLocal
        ) { username, email, avatar, token ->

            if (token.isNullOrEmpty()) {
                null
            } else {
                UserGoogle(
                    name = username,
                    email = email,
                    avatarUrl = avatar,
                    idToken = token,
                    authCode = null
                )
            }
        }

    override suspend fun logout(): Result<Unit> {
        firebaseDataSource.logout()
        sessionPreference.clearAll()
        return Result.success(Unit)
    }

    /*override suspend fun tryAutoLoginLocal() { // lógica opcional
    }*/
}
