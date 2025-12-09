package com.ajpr00.visumloop.tablet.presentation.state

sealed class LoginEvent {
    object Idle : LoginEvent()
    object Loading : LoginEvent()
    object EmailSuccess : LoginEvent()
    data class GoogleSuccess(val driveToken: String?) : LoginEvent()
    data class Error(val message: String?) : LoginEvent()
}
