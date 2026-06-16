package com.ajpr00.presentation_common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.core.domain.usecase.network.CheckInternetConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val isLoginStateUseCase: IsLoginStateUseCase,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase
): ViewModel() {

    val isLoggedIn = isLoginStateUseCase()

    private val _hasInternet = MutableStateFlow(false)
    val hasInternet: StateFlow<Boolean> = _hasInternet

    init {
        observeInternet()
    }

    private fun observeInternet() {
        viewModelScope.launch {
            _hasInternet.value = checkInternetConnectionUseCase()
        }
    }
}
