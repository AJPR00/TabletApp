package com.ajpr00.visumloop.tablet.data.repository

import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EmailAuthRepository @Inject constructor(
    private val loginPreferences: LoginPreferences

){

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Login con email/contraseña
    suspend fun loginEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            loginPreferences.saveLocalSession(username = email, email = email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}