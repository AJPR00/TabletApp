package com.ajpr00.visumloop.tablet.ui.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.presentation.state.LoginEvent
import com.ajpr00.visumloop.tablet.presentation.viewmodel.LoginViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.visumloop.tablet.ui.components.CustomButton
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    viewModelMediaItem: MediaItemsViewModel,
    goToMainGraph: () -> Unit,
    onLoginEmail: () -> Unit
) {
    Log.d("LoginUI", "🔄 LoginScreen recomposed")

    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val loginEvent by viewModel.loginEvent.collectAsState()

    var isEmail by rememberSaveable { mutableStateOf(false) }

    val launcherLogin = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result ->

        val response = result.idpResponse

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("LoginFlow", "Login correcto")
            val account = GoogleSignIn.getLastSignedInAccount(context)
            viewModel.onGoogleAccountReceived(account, context)

        } else {
            if (response == null) {
                Log.w("LoginFlow", "Login cancelado")
            } else {
                Log.e("LoginFlow", "Error de login: ${response.error?.errorCode}")
            }
        }
    }

    // -------------------------------
    // Iniciar Login Google
    // -------------------------------
    fun startGoogleLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder()
                .setScopes(listOf(DriveScopes.DRIVE_READONLY))
                .build()
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        launcherLogin.launch(intent)
    }

    // -------------------------------
    // UI
    // -------------------------------
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.app_nombre_t_tulo1),
                contentDescription = "Logo local"
            )

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEmail) {
                CustomButton(
                    icono = R.drawable.google_icon,
                    label = "Iniciar sesión con Google",
                    onClick = {
                        Log.d("LoginUI", "Google login pulsado")
                        startGoogleLogin()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEmail) {

                Text(
                    "Atrás",
                    modifier = Modifier
                        .clickable { isEmail = false }
                        .align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email ?: "",
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (state.showPassword)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (state.showPassword)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            CustomButton(
                icono = R.drawable.email_ic,
                label = if (!isEmail) "Iniciar sesión con Email" else "Iniciar sesión",
                onClick = {
                    if (!isEmail) {
                        isEmail = true
                    } else {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(state.email!!, state.password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    goToMainGraph()
                                } else {
                                    Toast.makeText(context,"Email o contraseña incorrectos",Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            )

            if (isEmail) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("¿No tienes una cuenta? Regístrate")
                Text("¿Olvidaste tu contraseña?")
            }

            Text(
                "Salir",
                modifier = Modifier.clickable { goToMainGraph() }
            )
        }
    }

    // -------------------------------
    // EVENTOS LOGIN
    // -------------------------------
    LaunchedEffect(loginEvent) {
        when (val ev = loginEvent) {

            is LoginEvent.GoogleSuccess -> {
                ev.driveToken?.let { token ->
                    viewModelMediaItem.setDriveToken(token)
                    viewModelMediaItem.loadFromDrive(token)
                }
                goToMainGraph()
                viewModel.clearEvents()
            }

            is LoginEvent.EmailSuccess -> {
                goToMainGraph()
                viewModel.clearEvents()
            }

            is LoginEvent.Error -> {
                Toast.makeText(context, ev.message ?: "Error login", Toast.LENGTH_SHORT).show()
                viewModel.clearEvents()
            }

            else -> {}
        }
    }
}
