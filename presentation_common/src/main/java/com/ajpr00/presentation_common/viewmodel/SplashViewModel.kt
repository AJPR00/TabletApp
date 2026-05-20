package com.ajpr00.presentation_common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.ObserveLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
@HiltViewModel
class SplashViewModel @Inject constructor(
    observeLoginStateUseCase: ObserveLoginStateUseCase
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> =
        observeLoginStateUseCase()
            .onEach { value ->
                Log.d("SplashViewModel", "isLoggedIn emitted: $value")
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
}

