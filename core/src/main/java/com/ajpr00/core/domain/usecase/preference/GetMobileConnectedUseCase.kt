package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class GetMobileConnectedUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    operator fun invoke() = repo.getMobileConnected()
}
