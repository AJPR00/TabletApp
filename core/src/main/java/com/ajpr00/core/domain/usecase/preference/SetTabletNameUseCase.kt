package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import javax.inject.Inject

class SetTabletNameUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    suspend operator fun invoke(name: String) = repo.setTabletName(name)
}
