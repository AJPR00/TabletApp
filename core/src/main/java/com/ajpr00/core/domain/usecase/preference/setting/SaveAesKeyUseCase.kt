package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class SaveAesKeyUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    suspend operator fun invoke(bytes: ByteArray) {
        repo.setAesKey(bytes)
    }
}