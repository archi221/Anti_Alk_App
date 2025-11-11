package com.example.soberup.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling user authentication via Firestore
 */
class AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val TAG = "AuthRepository"

    /**
     * Authenticates a user by checking name and password in Firestore (for debug)
     * @param name The name to authenticate
     * @param password The password to verify
     * @return User object if authentication succeeds, null otherwise
     */
    suspend fun loginByName(name: String, password: String): User? {
        return try {
            Log.d(TAG, "Attempting login for name: $name")
            
            // Query Firestore for a user with the given name
            val querySnapshot = usersCollection
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()

            Log.d(TAG, "Query completed. Found ${querySnapshot.size()} documents")

            if (querySnapshot.isEmpty) {
                Log.w(TAG, "No user found with name: $name")
                return null
            }

            // Get the first document (should be only one)
            val document = querySnapshot.documents[0]
            Log.d(TAG, "Document found: ${document.id}")
            Log.d(TAG, "Document data: ${document.data}")
            
            // Parse user data from Firestore
            val data = document.data
            val user = User(
                id = document.id,
                username = data?.get("username") as? String ?: "",
                password = data?.get("password") as? String ?: "",
                role = data?.get("role") as? String ?: "",
                name = data?.get("name") as? String ?: "",
                email = data?.get("email") as? String ?: "",
                soberDays = (data?.get("soberDays") as? Long)?.toInt() ?: 0,
                soberSince = data?.get("soberSince") as? com.google.firebase.Timestamp,
                sosContact = (data?.get("sosContact") as? Map<*, *>)?.let { map ->
                    SOSContact(
                        name = map["name"] as? String ?: "",
                        phone = map["phone"] as? String ?: ""
                    )
                },
                triggers = (data?.get("triggers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                assignedDoctorId = data?.get("assignedDoctorId") as? String ?: "",
                createdAt = data?.get("createdAt") as? com.google.firebase.Timestamp,
                lastMoodCheck = data?.get("lastMoodCheck") as? com.google.firebase.Timestamp
            )
            
            Log.d(TAG, "Parsed user: username=${user.username}, role=${user.role}, name=${user.name}")

            // Verify password
            if (user.password == password) {
                Log.d(TAG, "Login successful for user: ${user.name}")
                user
            } else {
                Log.w(TAG, "Password mismatch for user: $name")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks if a username exists in the database
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    suspend fun usernameExists(username: String): Boolean {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

