package com.ajpr00.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.tablet.domain.usecase.InitTabletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingTabletViewModel @Inject constructor(
    private val initTabletUseCase: InitTabletUseCase,
) : ViewModel() {

    var name: String = ""

    fun saveNameProvisonal(src: String) {
        name = src
    }

    fun setTabletName(name: String) {
        viewModelScope.launch {
            initTabletUseCase.invoke(name)
        }
    }
}
