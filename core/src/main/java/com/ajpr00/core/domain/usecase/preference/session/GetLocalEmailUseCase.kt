package com.ajpr00.core.domain.usecase.preference.session

import com.ajpr00.core.domain.repository.preference.SessionManager
import javax.inject.Inject

class GetLocalEmailUseCase @Inject constructor(
    private val repo: SessionManager
) {
    suspend operator fun invoke() = repo.getLocalEmail()
}