package il.co.erg.mykumve.data.db.firebasemvm.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

suspend fun deleteAllCollections(ignoreTables: List<String>? = null) {
    val db = FirebaseFirestore.getInstance()
    val collections = listOf("trips", "trip_invitations", "reports", "users")

    for (collection in collections) {
        if (ignoreTables == null || !ignoreTables.contains(collection)) {
            deleteCollection(db, collection)
        }
    }
}

suspend fun deleteCollection(db: FirebaseFirestore, collection: String) {
    val batchSize = 100 // Firestore allows batched deletions of up to 500 documents
    val collectionRef = db.collection(collection)
    var query: QuerySnapshot
    do {
        query = collectionRef.limit(batchSize.toLong()).get().await()
        val batch = db.batch()
        for (document in query.documents) {
            batch.delete(document.reference)
        }
        batch.commit().await()
    } while (query.size() == batchSize)
}
