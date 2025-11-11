package com.example.soberup.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing support locations in Firestore
 */
class SupportLocationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "SupportLocationRepository"

    /**
     * Get all support locations
     */
    suspend fun getAllSupportLocations(): List<SupportLocation> {
        return try {
            val snapshot = firestore.collection("supportLocations")
                .orderBy("name")
                .get()
                .await()

            snapshot.documents.map { doc ->
                val data = doc.data
                SupportLocation(
                    id = doc.id,
                    name = data?.get("name") as? String ?: "",
                    address = data?.get("address") as? String ?: "",
                    openingHours = data?.get("openingHours") as? String ?: "",
                    emergencyNumber = data?.get("emergencyNumber") as? String ?: "",
                    createdBy = data?.get("createdBy") as? String ?: "",
                    createdAt = data?.get("createdAt") as? Timestamp
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching support locations: ${e.message}", e)
            emptyList()
        }
    }
}

