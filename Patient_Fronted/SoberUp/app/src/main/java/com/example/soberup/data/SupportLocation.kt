package com.example.soberup.data

import com.google.firebase.Timestamp

/**
 * Data class representing a support location
 */
data class SupportLocation(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val openingHours: String = "",
    val emergencyNumber: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)

