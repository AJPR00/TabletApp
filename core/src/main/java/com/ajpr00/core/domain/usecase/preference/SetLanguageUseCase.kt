package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    suspend operator fun invoke(lang: String) {
        repo.setLanguage(lang)
    }
}
