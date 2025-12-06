package com.ajpr00.visumloop.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository // tu lógica de login/autenticación
) : ViewModel() {

    /*val uiState: StateFlow<SplashUiState> = authRepository.authState
        .map { isLoggedIn ->
            if (isLoggedIn) SplashUiState.GoToMain else SplashUiState.GoToLogin
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SplashUiState.Loading)*/
}

sealed interface SplashUiState {
    object Loading : SplashUiState
    object GoToLogin : SplashUiState
    object GoToMain : SplashUiState
}