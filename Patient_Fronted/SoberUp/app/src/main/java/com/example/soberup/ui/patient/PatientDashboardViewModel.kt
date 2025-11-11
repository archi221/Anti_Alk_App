package com.example.soberup.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soberup.data.MoodEntry
import com.example.soberup.data.MoodRepository
import com.example.soberup.data.User
import com.example.soberup.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class PatientDashboardViewModel(
    private val userId: String,
    private val moodRepository: MoodRepository = MoodRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _moodEntries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moodEntries: StateFlow<List<MoodEntry>> = _moodEntries.asStateFlow()

    private val _todayMood = MutableStateFlow<MoodEntry?>(null)
    val todayMood: StateFlow<MoodEntry?> = _todayMood.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserData()
        loadMoodEntries()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userData = userRepository.getUser(userId)
                _user.value = userData
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoodEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calendar = Calendar.getInstance()
                val entries = moodRepository.getMoodEntriesForMonth(
                    userId,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)
                )
                _moodEntries.value = entries

                // Load today's mood
                val today = Date()
                val todayEntry = moodRepository.getMoodEntryForDate(userId, today)
                _todayMood.value = todayEntry
            } catch (e: Exception) {
                _errorMessage.value = "Error loading mood entries: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveMood(moodValue: Int, note: String = "") {
        if (moodValue < 1 || moodValue > 10) {
            _errorMessage.value = "Mood value must be between 1 and 10"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = Date()
                val existingEntry = _todayMood.value

                val moodEntry = MoodEntry(
                    id = existingEntry?.id ?: "",
                    date = com.google.firebase.Timestamp(today),
                    moodValue = moodValue,
                    note = note,
                    createdAt = existingEntry?.createdAt ?: com.google.firebase.Timestamp.now()
                )

                val success = moodRepository.saveMoodEntry(userId, moodEntry)
                if (success) {
                    _todayMood.value = moodEntry
                    loadMoodEntries() // Reload to update calendar
                } else {
                    _errorMessage.value = "Failed to save mood entry"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error saving mood: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMoodForDate(date: Date): MoodEntry? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return _moodEntries.value.firstOrNull { entry ->
            entry.date?.toDate()?.let { entryDate ->
                val entryCalendar = Calendar.getInstance()
                entryCalendar.time = entryDate
                entryCalendar.set(Calendar.HOUR_OF_DAY, 0)
                entryCalendar.set(Calendar.MINUTE, 0)
                entryCalendar.set(Calendar.SECOND, 0)
                entryCalendar.set(Calendar.MILLISECOND, 0)
                entryCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                entryCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                entryCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
            } ?: false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update soberSince timestamp
     */
    fun updateSoberSince(soberSince: com.google.firebase.Timestamp) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.updateSoberSince(userId, soberSince)
                if (success) {
                    loadUserData() // Reload user data to get updated soberDays
                } else {
                    _errorMessage.value = "Fehler beim Aktualisieren des Datums"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mark that user has drunk (set soberSince to now, which will reset soberDays to 0)
     */
    fun markAsDrunk() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Set soberSince to now, which will make soberDays = 0
                val now = com.google.firebase.Timestamp.now()
                val success = userRepository.updateSoberSince(userId, now)
                if (success) {
                    loadUserData() // Reload user data
                } else {
                    _errorMessage.value = "Fehler beim Aktualisieren"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

