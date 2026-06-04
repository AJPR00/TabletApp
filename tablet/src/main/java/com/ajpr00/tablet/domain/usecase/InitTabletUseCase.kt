package com.ajpr00.tablet.domain.usecase

import com.ajpr00.tablet.data.datasource.local.preferences.AppPreference
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class InitTabletUseCase @Inject constructor(
    private val prefs: AppPreference
) {
    suspend operator fun invoke(name: String) {
        val firstRun = prefs.isFirstRun.first()

        if (firstRun) {
            val uuid = UUID.randomUUID().toString()
            prefs.setTabletId(uuid)
            prefs.setTabletName(name)
            prefs.setFirstRunCompleted()
        }
    }
}
