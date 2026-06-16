package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.repository.login.AuthRepository
import javax.inject.Inject

class FacebookAuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String) : Result<UserAuthData> =
        repository.loginWithFacebook(token)
}
