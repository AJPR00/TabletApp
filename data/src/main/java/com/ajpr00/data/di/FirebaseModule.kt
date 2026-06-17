package com.ajpr00.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseFirestoreModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("firestore.googleapis.com")   // 🔥 Fuerza REST
            .setSslEnabled(true)
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings
        return db
    }
}
