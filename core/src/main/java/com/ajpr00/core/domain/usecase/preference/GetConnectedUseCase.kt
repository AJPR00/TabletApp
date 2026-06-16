package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SessionManager
import javax.inject.Inject

class GetConnectedUseCase @Inject constructor(
    private val repo: SessionManager
) {
    suspend operator fun invoke() = repo.getLocalEmail()
}
