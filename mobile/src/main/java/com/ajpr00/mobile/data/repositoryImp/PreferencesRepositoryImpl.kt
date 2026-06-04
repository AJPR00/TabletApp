package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.mobile.data.datasource.local.preferences.AppPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val prefs: AppPreference
) : PreferencesRepository {

    override fun isDarkMode() = prefs.isDarkMode
    override suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)

    override fun getLanguage() = prefs.language
    override suspend fun setLanguage(lang: String) = prefs.setLanguage(lang)

    override fun isFirstRun() = prefs.isFirstRun
    override suspend fun setFirstRunCompleted() = prefs.setFirstRunCompleted()

    // No se utilizan en TabletApp
    override fun getTabletId(): Flow<String> = flowOf("")

    override suspend fun setTabletId(id: String) { /* no-op */ }

    override fun getTabletName(): Flow<String> = flowOf("")

    override suspend fun setTabletName(name: String) { /* no-op */ }
}
