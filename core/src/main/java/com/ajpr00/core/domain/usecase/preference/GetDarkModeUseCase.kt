package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class GetDarkModeUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    operator fun invoke() = repo.isDarkMode()
}
