package com.example.soberup.data

import com.google.firebase.Timestamp

/**
 * Data class representing a mood entry for a specific date
 */
data class MoodEntry(
    val id: String = "",
    val date: Timestamp? = null,
    val moodValue: Int = 0, // 1-10
    val note: String = "",
    val createdAt: Timestamp? = null
) {
    /**
     * Returns the color category for the mood value
     * Red (1-3): critical
     * Yellow (4-7): neutral
     * Green (8-10): stable
     */
    fun getMoodColor(): MoodColor {
        return when {
            moodValue <= 3 -> MoodColor.RED
            moodValue <= 7 -> MoodColor.YELLOW
            else -> MoodColor.GREEN
        }
    }
}

enum class MoodColor {
    RED,    // 1-3: critical
    YELLOW, // 4-7: neutral
    GREEN   // 8-10: stable
}

