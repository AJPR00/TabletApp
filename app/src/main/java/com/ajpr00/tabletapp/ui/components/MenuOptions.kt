package com.ajpr00.tabletapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel

@Composable
fun MenuOptions(viewModel: MediaBackgroundViewModel,onClose: () -> Unit) {
    val context = LocalContext.current

    // Launcher para imágenes y vídeos con permisos persistentes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        viewModel.onMediasSelected(context, uris)
    }

    PanelMenu(
        shape = RoundedCornerShape(10),
        oneBox = { /*TODO*/ },
        twoBox = { /*TODO*/ },
        threeBox = { /*TODO*/ },
        fourBox = { /*TODO*/ },
        fiveBox = { /*TODO*/ },
        sixBox = { /*TODO*/ },
        sevenBox = { launcher.launch(arrayOf("image/*", "video/*")) },
        eightBox = { /*TODO*/ },
        nineBox = { /*TODO*/ }
    )
}
