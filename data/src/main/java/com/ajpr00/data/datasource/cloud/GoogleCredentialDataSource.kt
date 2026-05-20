package com.ajpr00.data.datasource.cloud

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import com.ajpr00.data.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext

class GoogleCredentialDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Login manual con Google usando el selector de cuentas.
     * Credential Manager abrirá la UI del selector de Google.
     */
    suspend fun getGoogleSignInCredential(): CustomCredential {
        val option = GetSignInWithGoogleOption.Builder(
            BuildConfig.GOOGLE_CLIENT_ID).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option) // SOLO Google
            .build()

        // CredentialManager trabaja a nivel de sistema
        val manager = CredentialManager.create(context)
        val response = manager.getCredential(context, request) //Se obtiene un response que contiene la credencial

        Log.d("Login", "Obtenida CustomCredential de Google, tipo: ${response.credential.type}")

        return response.credential as CustomCredential
    }

    fun extractGoogleIdToken(credential: Credential): GoogleIdTokenCredential {
        return GoogleIdTokenCredential.createFrom(credential.data)
    }

    //Login Automatico con Google
    suspend fun getGoogleIdCredential(context: Context): CustomCredential {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val manager = CredentialManager.create(context)
        val response = manager.getCredential(context, request)

        return response.credential as CustomCredential
    }
}