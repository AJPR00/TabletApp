package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class SetMobileConnectedUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    suspend operator fun invoke(connected: Boolean) = repo.setMobileConnected(connected)
}
