package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    operator fun invoke(): Flow<String> = repo.getLanguage()
}
