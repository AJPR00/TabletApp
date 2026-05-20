package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.FacebookRepository
import javax.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(
    private val facebookRepository: FacebookRepository
)
 {
   /* suspend operator fun invoke(token: String): Result<Unit> {
        return repository.loginWithFacebook(token)
    }*/
}
