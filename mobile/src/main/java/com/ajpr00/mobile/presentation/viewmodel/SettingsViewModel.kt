package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.preference.setting.GetDarkModeUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetLanguageUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetIdUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetNameUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetDarkModeUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetLanguageUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetNameUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SaveAesKeyUseCase
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.mobile.presentation.state.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDarkMode: GetDarkModeUseCase,
    private val getLanguage: GetLanguageUseCase,
    private val getMobileId: GetIdUseCase,
    private val getMobileName: GetNameUseCase,
    private val isLoggedIn: IsLoginStateUseCase,
    private val setDarkMode: SetDarkModeUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val setMobileName: SetNameUseCase,
    private val saveAesKey: SaveAesKeyUseCase,
) : ViewModel() {

    private val TAG = "SettingsViewModel"

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                listOf(
                    getDarkMode(),
                    getLanguage(),
                    getMobileId(),
                    getMobileName(),
                    isLoggedIn(),
                )
            ) { values ->
                SettingsState(
                    isDarkMode = values[0] as Boolean,
                    language = values[1] as String,
                    movilId = values[2] as String,
                    movilName = values[3] as String,
                    isRedConnected = values[4] as Boolean,
                )
            }.collect { newState ->
                Log.d(TAG, "Nuevo estado recibido: $newState")
                _state.value = newState
            }
        }
    }

    // ---------------------------------------------------------
    // APARIENCIA
    // ---------------------------------------------------------

    fun toggleDarkMode() = viewModelScope.launch {
        setDarkMode()
    }

    fun changeLanguage(lang: String) = viewModelScope.launch {
        setLanguage(lang)
    }

    // ---------------------------------------------------------
    // IDENTIDAD DEL MÓVIL
    // ---------------------------------------------------------

    fun changeMobileName(name: String) = viewModelScope.launch {
        setMobileName(name)
    }

    // ---------------------------------------------------------
    // SEGURIDAD
    // ---------------------------------------------------------

    fun clearAesKey() = viewModelScope.launch {
        saveAesKey(ByteArray(0))
    }
}
