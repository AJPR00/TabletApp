package com.ajpr00.mobile.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.components.cards.MediaPreviewCard
import com.ajpr00.mobile.presentation.viewmodel.PanelControlViewModel
import com.ajpr00.mobile.ui.components.DispositivoCard
import com.ajpr00.mobile.ui.components.FabFloat
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.components.components.TextoConDivisor
import com.ajpr00.components.components.showToast
import com.ajpr00.components.screen.ScreenMediaExplorer
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.mobile.ui.components.RemoteMediaCard
import kotlin.collections.forEach

@Composable
fun ScreenPanelControl(
    modifier: Modifier = Modifier,
    viewModel: PanelControlViewModel = hiltViewModel(),
    onConfiguracion: () -> Unit,
    onAgregarDispositivo: () -> Unit,
) {
    val context = LocalContext.current


    val dispositivos by viewModel.dispositivosConEstado.collectAsState()
    val mediaList by viewModel.pendingMedia.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val state by viewModel.state.collectAsState()

    val isSelecId = selectedDevice != null

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    val launcherMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        viewModel.importSelectedMedia(uris)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        PanelMenu(
            modifier = modifier,
            isSelecId = isSelecId,
            dispositivos = dispositivos,
            listSendMedia = mediaList,
            selectedDevice = selectedDevice,
            onSelectDevice = { viewModel.selectDevice(it) },
            onSelectImage = { launcherMedia.launch(arrayOf("image/*")) },
            onSelectVideo = { launcherMedia.launch(arrayOf("video/*")) },
            onPendSend = viewModel::enviarMediaCifrado,
            openExploreListRepro = viewModel::loadListFav,
            onConfiguracion = onConfiguracion,
            onAgregarDispositivo = onAgregarDispositivo
        )

        if (state.showExplorerFav && isSelecId) {

            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f))
            {
                ScreenMediaExplorer<RemoteMedia>(
                    itemsFlow = viewModel.listReproServer,
                    label = "Lista de reproducción",
                    itemContent = { RemoteMediaCard(media = it) }
                )
                FabFloat(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .offset((-10).dp, 1.dp),
                    icon = painterResource(id = R.drawable.ic_close_small_24),
                    icDesc = "Agregar dispositivo",
                    onClick = { viewModel.closeExplorer()  }
                )
            }
        }
    }
}

@Composable
private fun PanelMenu(
    modifier: Modifier,
    isSelecId: Boolean = true,
    dispositivos: List<DispositivoUi>,
    listSendMedia: List<PendingMedia>,
    selectedDevice: DispositivoUi?,
    onSelectDevice: (DispositivoUi) -> Unit,
    onSelectImage: () -> Unit,
    onSelectVideo: () -> Unit,
    onPendSend: () -> Unit,
    openExploreListRepro: () -> Unit,
    onConfiguracion: () -> Unit,
    onAgregarDispositivo: () -> Unit,
) {
    val isListSendMedia = listSendMedia.isNotEmpty()
    val sizePanelButton = if (isListSendMedia) 0.50f else 0.75f

    Column(modifier = modifier.fillMaxSize()) {

        ActionGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(sizePanelButton),
            isEnable = isSelecId,
            onSelectImage = onSelectImage,
            onSelectVideo = onSelectVideo,
            openExploreListRepro = openExploreListRepro,
            onConfiguracion = onConfiguracion
        )

        // Lista de medias
        if (isListSendMedia) {
            MediaListSection(
                modifier = Modifier.weight(0.25f),
                icon = painterResource(id = R.drawable.ic_send_24),
                onFabClick = onPendSend,
                listSendMedia = listSendMedia
            )
        }

        // Lista de dispositivos
        DeviceListSection(
            modifier = Modifier.weight(0.25f),
            title = "Dispositivos disponibles:",
            listDispositivos = dispositivos,
            selectedDevice = selectedDevice,
            onSelectDevice = onSelectDevice,
            icon = painterResource(id = com.ajpr00.uicommon.R.drawable.ic_add_tablet24),
            onFabClick = onAgregarDispositivo
        )
    }
}

@Composable
private fun ActionGrid(
    modifier: Modifier,
    isEnable: Boolean = true,
    onSelectImage: () -> Unit,
    onSelectVideo: () -> Unit,
    openExploreListRepro: () -> Unit,
    onConfiguracion: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(0.8f),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(50.dp),
            verticalArrangement = Arrangement.spacedBy(50.dp),
        ) {
            item {
                ButtonCustonPanel(
                    icon = painterResource(id = R.drawable.ic_imag_up_24),
                    label = "Enviar Imagen",
                    isEnable = isEnable,
                    onClick = onSelectImage
                )
            }
            item {
                ButtonCustonPanel(
                    icon = painterResource(id = R.drawable.ic_video_add_24),
                    label = "Enviar Video",
                    isEnable = isEnable,
                    onClick = onSelectVideo
                )
            }
            item {
                ButtonCustonPanel(
                    icon = painterResource(id = R.drawable.outline_reviews_24),
                    label = "Ver lista reproducción",
                    isEnable = isEnable,
                    onClick = { openExploreListRepro() }
                )
            }
            item {
                ButtonCustonPanel(
                    icon = painterResource(id = R.drawable.ic_conf_24),
                    label = "Configuración",
                    isEnable = isEnable,
                    onClick = onConfiguracion
                )
            }
        }
    }
}

@Composable
private fun MediaListSection(
    modifier: Modifier,
    icon: Painter,
    onFabClick: () -> Unit,
    listSendMedia: List<PendingMedia>
) {
    Box(
        modifier = modifier
    ) {
        Column(modifier = modifier.fillMaxWidth()) {

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
                        size = 80.dp
                    )
                }
            }
        }
        FabFloat(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset((-10).dp, 1.dp),
            icon = icon,
            icDesc = "Agregar dispositivo",
            onClick = onFabClick
        )
    }
}


/**
 * DeviceListSection
 *
 * Esta sección muestra:
 *  - Un título ("Dispositivos disponibles")
 *  - Una lista horizontal de dispositivos (LazyRow)
 *  - Un FAB para añadir nuevos dispositivos
 *
 * Además, permite marcar un dispositivo como seleccionado.
 * Esto se hace comparando el ID del dispositivo con el ID del seleccionado.
 */
@Composable
fun DeviceListSection(
    modifier: Modifier = Modifier,
    sizeCard: Dp = 150.dp,
    title: String,
    icon: Painter,
    selectedDevice: DispositivoUi?,
    onSelectDevice: (DispositivoUi) -> Unit,
    listDispositivos: List<DispositivoUi>,
    onFabClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column {
            // Título con divisor (línea decorativa)
            TextoConDivisor(texto = title)

            if (listDispositivos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay dispositivos registrados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {

                // Lista horizontal de dispositivos
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(listDispositivos) { dispositivo ->

                        // Comprobamos si el dispositivo está seleccionado
                        val isSelected = selectedDevice?.id == dispositivo.id

                        DispositivoCard(
                            size = sizeCard,
                            dispositivo = dispositivo,
                            isSelected = isSelected,
                            onClick = { onSelectDevice(dispositivo) }
                        )
                    }
                }
            }
        }

        FabFloat(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset((-10).dp, 1.dp),
            icon = icon,
            icDesc = "Agregar dispositivo",
            onClick = onFabClick
        )
    }
}
