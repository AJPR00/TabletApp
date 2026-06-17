package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetnameMobileUseCase @Inject constructor(
    val repo: SettingsManager
) {
    operator fun invoke(): Flow<String> {
        return repo.getDeviceName()
    }
}