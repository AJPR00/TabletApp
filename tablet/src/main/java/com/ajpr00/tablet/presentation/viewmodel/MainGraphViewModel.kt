package com.ajpr00.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.preference.setting.GetIsFirstRunUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainGraphViewModel @Inject constructor(
    private val getIsFirstRunUseCase: GetIsFirstRunUseCase
) : ViewModel() {
    val isFirstRun = getIsFirstRunUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )
}