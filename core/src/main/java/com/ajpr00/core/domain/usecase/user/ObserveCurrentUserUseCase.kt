package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.repository.preference.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Observa el usuario actual combinando todos los datos de sesión.
 *
 * ## Estados posibles
 * - **null** → no hay usuario (sin username, sin email, sin token)
 * - **Usuario online** → email + idToken presentes
 * - **Usuario local** → username sin email/token (modo LAN)
 */
class ObserveCurrentUserUseCase @Inject constructor(
    private val repo: SessionManager
) {

    operator fun invoke(): Flow<UserAuthData?> =
        combine(
            repo.getLocalUsername(),
            repo.getLocalEmail(),
            repo.getLocalAvatar(),
            repo.getLocalIdToken(),
            repo.getLocalAuthCode()
        ) { username, email, avatar, token, authCode ->

            when {
                // 1. No hay identidad
                username.isBlank() && email.isBlank() && token.isNullOrBlank() -> null

                // 2. Usuario ONLINE (Google)
                email.isNotBlank() && !token.isNullOrBlank() -> UserAuthData(
                    username = username,
                    email = email,
                    avatarUrl = avatar,
                    token = token,
                    authCode = authCode
                )

                // 3. Usuario LOCAL (LAN)
                else -> UserAuthData(
                    username = username,
                    email = "",
                    avatarUrl = avatar,
                    token = token,
                )
            }
        }
}