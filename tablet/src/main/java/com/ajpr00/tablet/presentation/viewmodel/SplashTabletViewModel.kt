package com.ajpr00.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.repository.preference.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashTabletViewModel @Inject constructor(
    prefsRepository: SettingsManager
) : ViewModel() {
    val isFirstRun = prefsRepository.isFirstRun()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}