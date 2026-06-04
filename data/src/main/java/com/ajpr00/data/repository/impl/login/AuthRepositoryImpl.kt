package com.ajpr00.data.repository.impl.login

import com.ajpr00.core.domain.model.UserGoogle
import com.ajpr00.core.domain.repository.login.AuthPreference
import com.ajpr00.core.domain.repository.login.StateAuthRepository
import com.ajpr00.data.datasource.login.FirebaseAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authPreference: AuthPreference,
    private val firebaseDataSource: FirebaseAuthDataSource
) : StateAuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        authPreference.idTokenLocal.map { it != null }

    override val currentUser: Flow<UserGoogle?> =
        combine(
            authPreference.usernameLocal,
            authPreference.emailLocal,
            authPreference.avatarLocal,
            authPreference.idTokenLocal
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
        authPreference.clearAll()
        return Result.success(Unit)
    }
}
