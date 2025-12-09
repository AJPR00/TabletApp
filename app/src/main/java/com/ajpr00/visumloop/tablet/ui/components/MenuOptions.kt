package com.ajpr00.visumloop.tablet.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.ajpr00.visumloop.tablet.R

import com.ajpr00.visumloop.tablet.presentation.viewmodel.LoginViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MenuApp(
    viewModelMediaBackground: MediaBackgroundViewModel,
    viewModelMediaItme: MediaItemsViewModel,
    viewModelAuth: LoginViewModel,
    panelContent: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()  // CoroutineScope asociado al Composable
    val stateAuth = viewModelAuth.uiState.collectAsState().value

    val launcherMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->

        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        viewModelMediaItme.loadMediaLocal(context, uris)
    }
    //  PanelMenuOpciones(
    //  shape = RoundedCornerShape(10),
    //   oneBox = { /*TODO*/ },
    //   twoBox = { /*TODO*/ },
    //   threeBox = { },
    //    fourBox = { /*TODO*/ },
    //   fiveBox = { /*TODO*/ },
    //   sixBox = { viewModelMediaBackground.isShowSidePanel(it) },
    //    sevenBox = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
    //   eightBox = { viewModelMediaBackground.isShowSidePanel(it) },
    //   nineBox = {
    //       val token = viewModelAuth.driveToken
    //       viewModelMediaItme.loadFromDrive(token)
    //       token?.let { viewModelMediaItme.loadFromDrive(it) }
    //       viewModelMediaBackground.isShowSidePanel(it)
    //   },
    //   onLock = { viewModelMediaBackground.togglesLockScreen() },
    //  onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp(it) }
    //)

    panelContent()

    if (stateAuth.idToken != null) {
        Log.d("MenuApp", "Usuario autenticado con idToken=${stateAuth.idToken}")
        Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
    }
}



