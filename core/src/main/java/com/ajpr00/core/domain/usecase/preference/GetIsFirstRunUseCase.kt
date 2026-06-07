package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIsFirstRunUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isFirstRun()
    }
}
