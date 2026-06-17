package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject
class SetConnectedUseCase @Inject constructor(
    private val repo: SettingsManager

) {
    suspend operator fun invoke(connected: Boolean) = repo.setDeviceConnected(connected)
}
