package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetDarkModeUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    suspend operator fun invoke() {
        repo.setDarkMode(!repo.isDarkMode().first())
    }
}