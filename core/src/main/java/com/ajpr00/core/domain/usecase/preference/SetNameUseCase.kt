package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import javax.inject.Inject

class SetNameUseCase @Inject constructor(
    private val repo: SettingsManager
) {
    suspend operator fun invoke(name: String) = repo.setDeviceName(name)
}
