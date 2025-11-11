package com.example.soberup.data

import com.google.firebase.Timestamp

/**
 * Data class representing a user in the system
 * Matches the Firestore structure from context.md
 */
data class User(
    val id: String = "",
    val username: String = "", // For login
    val password: String = "", // For login (stored in Firestore)
    val role: String = "", // "patient", "doctor", or "admin"
    val name: String = "",
    val email: String = "",
    val soberDays: Int = 0,
    val soberSince: Timestamp? = null, // Timestamp when user became sober
    val sosContact: SOSContact? = null,
    val triggers: List<String> = emptyList(),
    val assignedDoctorId: String = "",
    val createdAt: Timestamp? = null,
    val lastMoodCheck: Timestamp? = null
) {
    /**
     * Calculate sober days based on soberSince timestamp
     */
    fun calculateSoberDays(): Int {
        return if (soberSince != null) {
            val now = com.google.firebase.Timestamp.now()
            val diff = now.seconds - soberSince.seconds
            val days = diff / (24 * 60 * 60)
            days.toInt().coerceAtLeast(0)
        } else {
            0
        }
    }
    
    /**
     * Check if user is a patient
     */
    fun isPatient(): Boolean = role == "patient"
    
    /**
     * Check if user is a doctor
     */
    fun isDoctor(): Boolean = role == "doctor"
    
    /**
     * Check if user is an admin
     */
    fun isAdmin(): Boolean = role == "admin"
}

