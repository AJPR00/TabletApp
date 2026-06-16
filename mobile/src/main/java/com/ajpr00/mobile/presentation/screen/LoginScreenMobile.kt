package com.ajpr00.mobile.presentation.screen

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.ajpr00.data.BuildConfig
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.components.screen.LoginScreenContent
import com.ajpr00.components.components.CustomButtonLogin
import com.ajpr00.visumloop.mobile.R
import androidx.credentials.GetCredentialRequest
import com.ajpr00.components.components.showToast
import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreenMobile(
    modifier: Modifier = Modifier,
    viewModelLogin: LoginViewModel,
    viewModelAuth: AuthViewModel,
    onFacebookLogin: () -> Unit,
    goToMainGraph: () -> Unit,
    goToFromRegistro: () -> Unit,
    goToRecuperarPass: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModelLogin.uiState.collectAsState()
    val event by viewModelLogin.State.collectAsState()

    var isEmail by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModelLogin.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        LoginScreenContent(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            logo = painterResource(id = R.drawable.imag_splash_control),
            email = state.email ?: "",
            password = state.password,
            showPassword = state.showPassword,
            isEmailMode = isEmail,
            onEmailModeToggle = { isEmail = !isEmail },
            onEmailChange = viewModelLogin::updateEmail,
            onPasswordChange = viewModelLogin::updatePassword,
            onTogglePasswordVisibility = viewModelLogin::togglePasswordVisibility,
            onLoginWithGoogle = {scope.launch { actCredentialManager(context, viewModelAuth)}},
            onLoginWithFacebook = onFacebookLogin,
            onLoginWithEmail = { /* ... */ },
            onGoToRegister = goToFromRegistro,
            onGoToRecoverPassword = goToRecuperarPass,
            onExit = goToMainGraph,
            loading = event is Estado.Cargando,
        )

        HorizontalDivider(modifier = Modifier.padding(16.dp))

        CustomButtonLogin(
            icono = R.drawable.ic_acceso_como_local,
            label = "Acceder Local",
            onClick = { }
        )
    }
}

private suspend fun actCredentialManager(
    context: Context,
    viewModel: AuthViewModel
) {
    val credentialManager = CredentialManager.create(context)

    val option = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    val result = credentialManager.getCredential(context, request)
    val googleCred = GoogleIdTokenCredential.createFrom(result.credential.data)

    viewModel.saveSession(
        UserAuthData(
        token = googleCred.idToken,
        email= googleCred.id,
        username = googleCred.displayName,
        avatarUrl = googleCred.profilePictureUri?.toString().orEmpty())
    )
}



