package com.ajpr00.core.domain.usecase.preference.setting

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class GetNameUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    operator fun invoke() = repo.getDeviceName()
}
