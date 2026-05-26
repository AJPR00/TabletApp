package com.ajpr00.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ajpr00.visumloop.mobile.R
import com.ajpr00.components.components.ButtonCustonPanel
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.components.cards.MediaPreviewCard
import com.ajpr00.mobile.presentation.viewmodel.PanelControlViewModel
import com.ajpr00.mobile.ui.components.DispositivoCard
import com.ajpr00.mobile.ui.components.FabAdd
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.components.components.TextoConDivisor
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.PendingMedia

@Composable
fun ScreenPanelControl(
    modifier: Modifier = Modifier,
    viewModel: PanelControlViewModel = hiltViewModel(),
    onSelectImage: () -> Unit,
    onSelectVideo: () -> Unit,
    onEnviar: () -> Unit,
    onVerArchivos: () -> Unit,
    onConfiguracion: () -> Unit,
    onAgregarDispositivo: () -> Unit,
) {

    val dispositivosPrueba = listOf(
        DispositivoUi(
            id = "1",
            nombre = "Tablet Sala",
            nivelBatery = 50,
            icono = R.drawable.ic_tablet,
            estado = EstadoDispositivo.ONLINE
        ),
        DispositivoUi(
            id = "1",
            nombre = "Tablet Sala",
            nivelBatery = 100,
            icono = R.drawable.ic_table_disabled,
            estado = EstadoDispositivo.OFFLINE
        )
    )


    val dispositivos by viewModel.dispositivos.collectAsState()
    val mediaList by viewModel.pendingMedia.collectAsState()


    /**
     * Este composable representa el MENÚ PRINCIPAL de la app.
     * Está organizado en una columna vertical con 4 botones grandes
     * y debajo una lista de dispositivos detectados.
     *
     * Cada botón usa el componente reutilizable ButtonCustonPanel.
     * Esto permite mantener un estilo uniforme en toda la app.
     */
    PanelMenu(
        modifier = modifier,
        numGrid = 2,
        spaceHori = 16,
        spaceVert = 16,
        onSelectImage = onSelectImage,
        onSelectVideo = onSelectVideo,
        onEnviar = onEnviar,
        onVerArchivos = onVerArchivos,
        onConfiguracion = onConfiguracion,
        addDispositivo = onAgregarDispositivo,
        dispositivos = dispositivos,
        listSendMedia = mediaList
    )
}

@Composable
private fun PanelMenu(
    modifier: Modifier,
    numGrid: Int,
    spaceHori: Int,
    spaceVert: Int,
    onSelectImage: () -> Unit,
    onSelectVideo: () -> Unit,
    onEnviar: () -> Unit,
    onVerArchivos: () -> Unit,
    onConfiguracion: () -> Unit,
    addDispositivo: () -> Unit,
    dispositivos: List<DispositivoUi>,
    listSendMedia: List<PendingMedia>,
) {
    val isListSendMedia = listSendMedia.isEmpty()
    val sizePanelButton = if (!isListSendMedia) 0.50f else 0.75f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val size = minOf(maxWidth, maxHeight)
        val sizeIcon = size / 4

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(sizePanelButton),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth(0.8f),
                    columns = GridCells.Fixed(numGrid),
                    horizontalArrangement = Arrangement.spacedBy(spaceHori.dp),
                    verticalArrangement = Arrangement.spacedBy(spaceVert.dp),
                ) {
                    item {
                        ButtonCustonPanel(
                            iconSize = sizeIcon,
                            icon = painterResource(id = R.drawable.ic_mobile_control),
                            label = "Enviar Imagen",
                            onClick = onSelectImage
                        )
                    }
                    item {
                        ButtonCustonPanel(
                            iconSize = sizeIcon,
                            icon = painterResource(id = R.drawable.ic_mobile_control),
                            label = "Enviar Video",
                            onClick = onSelectVideo
                        )
                    }
                    item {
                        ButtonCustonPanel(
                            iconSize = sizeIcon,
                            icon = painterResource(id = R.drawable.ic_mobile_control),
                            label = "Ver lista reproducion",
                            onClick = onVerArchivos
                        )
                    }
                    item {
                        ButtonCustonPanel(
                            iconSize = sizeIcon,
                            icon = painterResource(id = R.drawable.ic_mobile_control),
                            label = "Configuración del Dispositivo",
                            onClick = onConfiguracion
                        )
                    }
                }
            }

            // LISTA DE MEDIAS (solo si existe)
            if (!isListSendMedia) {

                Column(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth()
                ) {

                    TextoConDivisor(texto = "Medias a enviar:")

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        items(listSendMedia) { media ->

                            MediaPreviewCard(
                                media = media,
                                size = size * 0.35f,   // tamaño proporcional
                            )
                        }
                    }
                }
            }

            // LISTA DE DISPONIBLES (siempre)
            DeviceListSection(
                modifier = Modifier.weight(0.25f),
                size = size,
                title = "Dispositivos disponibles:",
                onFabClick = addDispositivo,
                icon = painterResource(id = com.ajpr00.uicommon.R.drawable.ic_add_tablet24),
                dispositivos = dispositivos
            )
        }
    }
}

@Composable
fun DeviceListSection(
    modifier: Modifier = Modifier,
    size: Dp,
    title: String,
    dispositivos: List<DispositivoUi>,
    icon: Painter,
    onFabClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        Column {
            TextoConDivisor(texto = title)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(dispositivos) { dispositivo ->
                    DispositivoCard(
                        size = size,
                        dispositivo = dispositivo
                    )
                }
            }
        }

        FabAdd(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset((-10).dp, 1.dp),
            icon = icon,
            icDesc = "Agregar dispositivo",
            onClick = onFabClick ?: {}
        )
    }
}