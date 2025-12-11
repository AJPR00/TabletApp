package com.ajpr00.visumloop.tablet.data.repository

import android.util.Log
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthRepository @Inject constructor(
    private val loginPreferences: LoginPreferences
) {
    val isLocalLoggedIn: Flow<Boolean> = loginPreferences.idTokenLocal
        .map { !it.isNullOrEmpty() }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    companion object {
        private const val TAG = "GoogleAuthRepository"
    }

    // Login con Google
    suspend fun loginWithGoogleFirebase(idToken: String): Result<Unit> {
        return try {
            Log.d(TAG, "Intentando login en Firebase con Google, idToken: $idToken")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Log.d(TAG, "Login exitoso en Firebase con Google: ${firebaseAuth.currentUser?.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error login Firebase con Google", e)
            Result.failure(e)
        }
    }

    // Guardar sesión Drive
    suspend fun saveDriveSession(email: String, idToken: String?) {
        Log.d(TAG, "Guardando sesión Drive: email=$email, token=${idToken?.take(10)}...")
        loginPreferences.saveDriveSession(email = email, idToken = idToken)
    }

    // Guardar sesión Google local
    suspend fun saveGoogleLocal(email: String, idToken: String?) {
        Log.d(TAG, "Guardando sesión local Google: email=$email, token=${idToken?.take(10)}...")
        loginPreferences.saveLocalSession(username = email, email = email, idToken = idToken)
    }

    // Logout general (Firebase)
    fun logout() {
        Log.d(TAG, "Realizando logout en Firebase")
        firebaseAuth.signOut()
    }

    // Logout específico Local
    suspend fun logoutLocal() {
        Log.d(TAG, "Realizando logout local y Firebase")
        firebaseAuth.signOut()
        loginPreferences.clearLocalSession()
    }

    // Logout específico Drive
    suspend fun logoutDrive() {
        Log.d(TAG, "Realizando logout Drive y Firebase")
        firebaseAuth.signOut()
        loginPreferences.clearDriveSession()
    }

    // Logout total (Firebase + DataStore completo)
    suspend fun logoutAll() {
        Log.d(TAG, "Realizando logout total (Firebase + DataStore)")
        firebaseAuth.signOut()
        loginPreferences.clearAll()
    }
}

