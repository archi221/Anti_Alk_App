package com.example.soberup.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

/**
 * Repository for managing mood entries in Firestore
 */
class MoodRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "MoodRepository"

    /**
     * Get mood entries for a user within a date range
     */
    suspend fun getMoodEntries(
        userId: String,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<MoodEntry> {
        return try {
            val collection = firestore
                .collection("users")
                .document(userId)
                .collection("moodEntries")

            var query = collection.orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)

            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("date", Timestamp(startDate))
            }
            if (endDate != null) {
                query = query.whereLessThanOrEqualTo("date", Timestamp(endDate))
            }

            val snapshot = query.get().await()
            snapshot.documents.map { doc ->
                val data = doc.data
                MoodEntry(
                    id = doc.id,
                    date = data?.get("date") as? Timestamp,
                    moodValue = (data?.get("moodValue") as? Long)?.toInt() ?: 0,
                    note = data?.get("note") as? String ?: "",
                    createdAt = data?.get("createdAt") as? Timestamp
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mood entries: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get mood entry for a specific date
     */
    suspend fun getMoodEntryForDate(userId: String, date: Date): MoodEntry? {
        return try {
            val startOfDay = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfDay = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val entries = getMoodEntries(
                userId,
                startDate = startOfDay,
                endDate = endOfDay
            )
            entries.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mood entry for date: ${e.message}", e)
            null
        }
    }

    /**
     * Save or update a mood entry
     */
    suspend fun saveMoodEntry(userId: String, moodEntry: MoodEntry): Boolean {
        return try {
            val collection = firestore
                .collection("users")
                .document(userId)
                .collection("moodEntries")

            val data = hashMapOf<String, Any>(
                "moodValue" to moodEntry.moodValue,
                "note" to (moodEntry.note.ifEmpty { "" })
            )

            if (moodEntry.date != null) {
                data["date"] = moodEntry.date
            } else {
                data["date"] = Timestamp.now()
            }

            if (moodEntry.createdAt != null) {
                data["createdAt"] = moodEntry.createdAt
            } else {
                data["createdAt"] = Timestamp.now()
            }

            if (moodEntry.id.isNotEmpty()) {
                // Update existing entry
                collection.document(moodEntry.id).set(data).await()
            } else {
                // Create new entry
                collection.add(data).await()
            }

            // Update user's lastMoodCheck
            firestore.collection("users").document(userId)
                .update("lastMoodCheck", Timestamp.now())
                .await()

            Log.d(TAG, "Mood entry saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entry: ${e.message}", e)
            false
        }
    }

    /**
     * Get mood entries for a specific month
     */
    suspend fun getMoodEntriesForMonth(userId: String, year: Int, month: Int): List<MoodEntry> {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        return getMoodEntries(userId, startDate, endDate)
    }
}

