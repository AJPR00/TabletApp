package com.ajpr00.mobile.qr

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.mobile.data.mapper.toDomain
import com.ajpr00.mobile.qr.parse.QrParser
import javax.inject.Inject

/**
 * Procesa el contenido bruto del QR y lo convierte en un `Dispositivo`.
 *
 * Rol:
 * - Orquestar parser + mapper.
 * - Pertenece a la capa de aplicación (no dominio puro).
 * - Mantiene la lógica QR fuera del ViewModel.
 */
class QrProcessor @Inject constructor(
    private val parser: QrParser
) {

    operator fun invoke(raw: String): QrPayload {
        return parser.parse(raw)
    }
}
