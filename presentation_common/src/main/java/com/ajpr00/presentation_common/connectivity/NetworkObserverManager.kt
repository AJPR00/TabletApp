package com.ajpr00.presentation_common.connectivity

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Manager que encapsula la creación y registro del NetworkObserver.
 * Permite que el ViewModel controle la lógica sin tener acceso al Context.
 */
class NetworkObserverManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun start(onChange: (Boolean) -> Unit): NetworkObserver {
        return NetworkObserver(context, onChange).also { it.register() }
    }
}
