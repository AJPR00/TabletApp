package com.ajpr00.tablet.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.components.components.showToast
import com.ajpr00.tablet.R
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.components.screen.LoginScreenContent
import com.ajpr00.data.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.presentation_common.viewmodel.AuthViewModel

@Composable
fun LoginScreenTablet(
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

    LoginScreenContent(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        logo = painterResource(id = R.drawable.app_nombre_t_tulo1),
        email = state.email ?: "",
        password = state.password,
        showPassword = state.showPassword,
        isEmailMode = isEmail,
        onEmailModeToggle = { isEmail = !isEmail },
        onEmailChange = viewModelLogin::updateEmail,
        onPasswordChange = viewModelLogin::updatePassword,
        onTogglePasswordVisibility = viewModelLogin::togglePasswordVisibility,
        onLoginWithGoogle = {scope.launch {actCredentialManager( context,viewModelAuth) }},
        onLoginWithFacebook = onFacebookLogin,
        onLoginWithEmail = { /* ... */ },
        onGoToRegister = goToFromRegistro,
        onGoToRecoverPassword = goToRecuperarPass,
        onExit = goToMainGraph,
        loading = event is Estado.Cargando,
    )
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



