package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.repository.preference.SessionManager
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: SessionManager
) {
   suspend operator fun invoke(data: UserAuthData) {
        repository.saveLocalSession(data)
    }
}