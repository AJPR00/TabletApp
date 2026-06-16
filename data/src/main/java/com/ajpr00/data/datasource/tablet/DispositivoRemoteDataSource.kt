package com.ajpr00.data.datasource.tablet

import com.ajpr00.data.dto.dispositivo.TabletFirestoreDto
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class DispositivoRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    class TabletFirestoreDataSource @Inject constructor(
        private val firestore: FirebaseFirestore
    ) {

        suspend fun getTabletById(idTablet: String): TabletFirestoreDto? {
            return firestore.collection("tablets")
                .document(idTablet)
                .get()
                .await()
                .toObject(TabletFirestoreDto::class.java)
        }
    }

}
