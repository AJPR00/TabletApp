package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class SaveAesKeyUseCase @Inject constructor(
    private val prefs: PreferencesRepository
) {
    suspend operator fun invoke(bytes: ByteArray) {
        prefs.setAesKey(bytes)
    }
}
