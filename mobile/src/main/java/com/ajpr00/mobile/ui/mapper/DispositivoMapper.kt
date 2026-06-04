package com.ajpr00.mobile.ui.mapper

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R

fun Dispositivo.toDispositivoUi(): DispositivoUi {
    return DispositivoUi(
        id = id,
        ip = ip,
        puerto = puerto,
        nombre = nombre,
        nivelBatery = nivelBatery,
        icono = when (estado) {
            EstadoDispositivo.ONLINE -> R.drawable.ic_tablet
            EstadoDispositivo.OFFLINE -> R.drawable.ic_table_disabled
            EstadoDispositivo.DESCONOCIDO -> R.drawable.ic_tablet_nknown
        },
        estado = estado
    )
}
