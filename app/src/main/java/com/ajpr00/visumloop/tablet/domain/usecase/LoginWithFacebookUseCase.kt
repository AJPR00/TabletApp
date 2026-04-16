package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.FacebookRepository
import jakarta.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(
    private val facebookRepository: FacebookRepository
)
 {
   /* suspend operator fun invoke(token: String): Result<Unit> {
        return repository.loginWithFacebook(token)
    }*/
}
