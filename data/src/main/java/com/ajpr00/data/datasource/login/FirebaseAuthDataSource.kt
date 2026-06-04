package com.ajpr00.data.datasource.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun loginWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun registerEmail(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun userExists(email: String): Boolean {
        val result = firebaseAuth.fetchSignInMethodsForEmail(email).await()
        return !result.signInMethods.isNullOrEmpty()
    }

    suspend fun sendPasswordReset(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun loginWithIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun getCurrentUserToken(): String {
        val user = firebaseAuth.currentUser ?: return ""
        return user.getIdToken(true).await()?.token.orEmpty()
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isFirebaseLoggedIn(): Boolean = firebaseAuth.currentUser != null
    fun loginWithFacebook(token: String) { }

    fun loginWithDropbox(token: String) { }
}
