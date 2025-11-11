package com.example.soberup.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing user data in Firestore
 */
class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "UserRepository"

    /**
     * Update user data in Firestore
     */
    suspend fun updateUser(user: User): Boolean {
        return try {
            val data = hashMapOf<String, Any>(
                "username" to user.username,
                "role" to user.role,
                "name" to user.name,
                "email" to user.email,
                "soberDays" to user.soberDays,
                "triggers" to user.triggers,
                "assignedDoctorId" to user.assignedDoctorId
            )

            if (user.sosContact != null) {
                data["sosContact"] = hashMapOf(
                    "name" to user.sosContact.name,
                    "phone" to user.sosContact.phone
                )
            }

            if (user.createdAt != null) {
                data["createdAt"] = user.createdAt
            }

            if (user.lastMoodCheck != null) {
                data["lastMoodCheck"] = user.lastMoodCheck
            }

            if (user.soberSince != null) {
                data["soberSince"] = user.soberSince
            }

            firestore.collection("users").document(user.id).update(data).await()
            Log.d(TAG, "User updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}", e)
            false
        }
    }

    /**
     * Update SOS contact for a user
     */
    suspend fun updateSOSContact(userId: String, sosContact: SOSContact): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update(
                    "sosContact",
                    hashMapOf(
                        "name" to sosContact.name,
                        "phone" to sosContact.phone
                    )
                )
                .await()
            Log.d(TAG, "SOS contact updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating SOS contact: ${e.message}", e)
            false
        }
    }

    /**
     * Update triggers for a user
     */
    suspend fun updateTriggers(userId: String, triggers: List<String>): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update("triggers", triggers)
                .await()
            Log.d(TAG, "Triggers updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating triggers: ${e.message}", e)
            false
        }
    }

    /**
     * Update sober days for a user
     */
    suspend fun updateSoberDays(userId: String, soberDays: Int): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update("soberDays", soberDays)
                .await()
            Log.d(TAG, "Sober days updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating sober days: ${e.message}", e)
            false
        }
    }

    /**
     * Update soberSince timestamp for a user
     */
    suspend fun updateSoberSince(userId: String, soberSince: Timestamp): Boolean {
        return try {
            val calculatedDays = calculateSoberDays(soberSince)
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "soberSince" to soberSince,
                        "soberDays" to calculatedDays
                    )
                )
                .await()
            Log.d(TAG, "SoberSince updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating soberSince: ${e.message}", e)
            false
        }
    }

    /**
     * Calculate sober days from a timestamp
     */
    private fun calculateSoberDays(soberSince: Timestamp): Int {
        val now = Timestamp.now()
        val diff = now.seconds - soberSince.seconds
        val days = diff / (24 * 60 * 60)
        return days.toInt().coerceAtLeast(0)
    }

    /**
     * Get user by ID
     */
    suspend fun getUser(userId: String): User? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val data = document.data
                User(
                    id = document.id,
                    username = data?.get("username") as? String ?: "",
                    password = data?.get("password") as? String ?: "",
                    role = data?.get("role") as? String ?: "",
                    name = data?.get("name") as? String ?: "",
                    email = data?.get("email") as? String ?: "",
                    soberDays = (data?.get("soberDays") as? Long)?.toInt() ?: 0,
                    soberSince = data?.get("soberSince") as? Timestamp,
                    sosContact = (data?.get("sosContact") as? Map<*, *>)?.let { map ->
                        SOSContact(
                            name = map["name"] as? String ?: "",
                            phone = map["phone"] as? String ?: ""
                        )
                    },
                    triggers = (data?.get("triggers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    assignedDoctorId = data?.get("assignedDoctorId") as? String ?: "",
                    createdAt = data?.get("createdAt") as? Timestamp,
                    lastMoodCheck = data?.get("lastMoodCheck") as? Timestamp
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user: ${e.message}", e)
            null
        }
    }
}

