package com.ajpr00.presentation_common.state

sealed class Estado {
    object Exito : Estado()
    object Cargando : Estado()
    object Inicial : Estado()
}