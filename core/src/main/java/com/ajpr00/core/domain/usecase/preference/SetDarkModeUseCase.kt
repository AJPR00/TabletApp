package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class SetDarkModeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }
}
