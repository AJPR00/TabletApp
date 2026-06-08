package com.ajpr00.tablet.presentation.state

/**
 * Estado completo del proceso de emparejamiento.
 *
 * @property pin PIN generado por el servidor.
 * @property showPinDialog Indica si debe mostrarse el Dialog del PIN.
 * @property isPaired Indica si el móvil ya validó el PIN.
 * @property isLoading Indica si se está realizando una operación de red.
 * @property error Mensaje de error si algo falla.
 */
data class PairingState(
    val pin: String = "",
    val showPinDialog: Boolean = false,
    val isPaired: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
