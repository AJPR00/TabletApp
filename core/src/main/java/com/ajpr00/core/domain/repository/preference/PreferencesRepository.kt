package com.ajpr00.core.domain.repository.preference

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)

    fun getLanguage(): Flow<String>
    suspend fun setLanguage(lang: String)

    fun isFirstRun(): Flow<Boolean>
    suspend fun setFirstRunCompleted()

    fun getTabletId(): Flow<String>
    suspend fun setTabletId(id: String)

    fun getTabletName(): Flow<String>
    suspend fun setTabletName(name: String)

    suspend fun setAesKey(bytes: ByteArray)
    fun getAesKey(): Flow<ByteArray?>
}
