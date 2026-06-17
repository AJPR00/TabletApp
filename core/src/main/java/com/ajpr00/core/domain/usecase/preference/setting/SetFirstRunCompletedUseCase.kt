package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class SetFirstRunCompletedUseCase @Inject constructor(
    private val repo: SettingsManager

) {
    suspend operator fun invoke() {
        repo.setFirstRunCompleted()
    }
}
