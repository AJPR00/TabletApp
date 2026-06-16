package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAesKeyUseCase @Inject constructor(
    val repo: SettingsManager
) {
    suspend operator fun invoke(): ByteArray? {
        return repo.getAesKey().first()

    }
}