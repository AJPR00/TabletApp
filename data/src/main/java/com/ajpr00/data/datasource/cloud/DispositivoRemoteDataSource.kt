package com.ajpr00.data.datasource.cloud

import com.ajpr00.core.domain.model.Dispositivo
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DispositivoRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getDispositivos(): Flow<List<Dispositivo>> = callbackFlow {
        val listener = firestore.collection("dispositivos")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Dispositivo::class.java))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun addDispositivo(dispositivo: Dispositivo) {
        firestore.collection("dispositivos")
            .document(dispositivo.id)
            .set(dispositivo)
    }
}
