package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(lang: String) {
        repository.setLanguage(lang)
    }
}
