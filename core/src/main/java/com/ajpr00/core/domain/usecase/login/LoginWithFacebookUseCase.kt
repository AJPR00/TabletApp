package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.repository.login.FacebookAuthRepository
import javax.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(
    private val facebookRepository: FacebookAuthRepository
)
 {
   /* suspend operator fun invoke(token: String): Result<Unit> {
        return repository.loginWithFacebook(token)
    }*/
}
