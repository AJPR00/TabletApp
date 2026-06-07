package com.ajpr00.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.tablet.domain.usecase.InitTabletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # OnboardingTabletViewModel
 *
 * ViewModel encargado de gestionar el flujo de **onboarding inicial** de la tablet.
 *
 * ## ¿Qué hace este ViewModel?
 * - Mantiene en memoria el nombre provisional que escribe el usuario.
 * - Expone una función para confirmar el nombre y ejecutar la inicialización real.
 *
 * ## Relación con otras capas
 * - **presentation**: la pantalla de onboarding escribe en `name` y llama a `setTabletName()`.
 * - **domain**: delega la inicialización en `InitTabletUseCase`.
 * - **data**: el caso de uso escribe en `PreferencesRepository`.
 *
 * ## Flujo típico
 * 1. El usuario escribe un nombre.
 * 2. La UI llama a `saveNameProvisional()`.
 * 3. El usuario confirma.
 * 4. La UI llama a `setTabletName()`.
 * 5. El caso de uso genera `tabletId`, guarda el nombre y marca el onboarding como completado.
 */
@HiltViewModel
class OnboardingTabletViewModel @Inject constructor(
    private val initTabletUseCase: InitTabletUseCase,
) : ViewModel() {

    /**
     * Nombre provisional introducido por el usuario.
     * La UI lo actualiza en tiempo real.
     */
    var name: String = ""

    /**
     * Guarda temporalmente el nombre mientras el usuario escribe.
     *
     * @param src Texto introducido en el campo de nombre.
     */
    fun saveNameProvisonal(src: String) {
        name = src
    }

    /**
     * Confirma el nombre introducido y ejecuta el caso de uso de inicialización.
     *
     * ## Flujo interno
     * - Lanza una corrutina.
     * - Llama a `InitTabletUseCase`.
     * - El caso de uso genera el `tabletId`, guarda el nombre y marca el onboarding como completado.
     *
     * @param name Nombre definitivo elegido por el usuario.
     */
    fun setTabletName(name: String) {
        viewModelScope.launch {
            initTabletUseCase.invoke(name)
        }
    }
}
