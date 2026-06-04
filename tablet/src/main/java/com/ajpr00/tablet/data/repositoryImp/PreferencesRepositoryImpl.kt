package com.ajpr00.tablet.data.repositoryImp

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.tablet.data.datasource.local.preferences.AppPreference
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

}
