package com.example.soberup.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soberup.data.MoodColor
import com.example.soberup.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    userId: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupportLocations: () -> Unit,
    onNavigateToTriggers: () -> Unit,
    viewModel: PatientDashboardViewModel = viewModel(factory = PatientDashboardViewModelFactory(userId))
) {
    val user by viewModel.user.collectAsState()
    val moodEntries by viewModel.moodEntries.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedMood by remember { mutableStateOf(todayMood?.moodValue ?: 0) }
    var moodNote by remember { mutableStateOf(todayMood?.note ?: "") }
    
    // State for sober days dialog
    var showSoberDaysDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(todayMood) {
        selectedMood = todayMood?.moodValue ?: 0
        moodNote = todayMood?.note ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SoberUp") },
                actions = {
                    IconButton(
                        onClick = {
                            onNavigateToProfile()
                        }
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(
                        onClick = {
                            onNavigateToSettings()
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSupportLocations,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Support Locations")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Welcome section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Willkommen, ${user?.name ?: "Patient"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Wie fühlst du dich heute?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Sober Days Counter
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSoberDaysDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nüchterne Tage",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${user?.calculateSoberDays() ?: 0}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (user?.soberSince != null) {
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                val soberSinceDate = user?.soberSince?.toDate()
                                soberSinceDate?.let {
                                    Text(
                                        text = "Nüchtern seit: ${dateFormat.format(it)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Today's Mood Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Heutige Stimmung",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Mood selector (1-10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (1..10).forEach { value ->
                                val moodColor = when {
                                    value <= 3 -> MoodColor.RED
                                    value <= 7 -> MoodColor.YELLOW
                                    else -> MoodColor.GREEN
                                }
                                
                                val color = when (moodColor) {
                                    MoodColor.RED -> Color(0xFFFF5252)
                                    MoodColor.YELLOW -> Color(0xFFFFEB3B)
                                    MoodColor.GREEN -> Color(0xFF4CAF50)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedMood == value) color
                                            else color.copy(alpha = 0.3f)
                                        )
                                        .clickable { selectedMood = value },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$value",
                                        color = if (selectedMood == value) Color.White else Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedMood == value) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Note field
                        OutlinedTextField(
                            value = moodNote,
                            onValueChange = { moodNote = it },
                            label = { Text("Notiz (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save button
                        Button(
                            onClick = {
                                if (selectedMood > 0) {
                                    viewModel.saveMood(selectedMood, moodNote)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedMood > 0 && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Stimmung speichern")
                            }
                        }
                    }
                }
            }

            // Mood Calendar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Kalender",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        MoodCalendarView(
                            moodEntries = moodEntries,
                            onDateClick = { date ->
                                // TODO: Show mood details for selected date
                            }
                        )
                    }
                }
            }

            // SOS Indicator
            item {
                val sosContact = user?.sosContact
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (sosContact != null) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (sosContact != null) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SOS-Kontakt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (sosContact != null) {
                                    "${sosContact.name}\n${sosContact.phone}"
                                } else {
                                    "Kein SOS-Kontakt hinterlegt"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                        }
                    }
                }
            }

            // Triggers Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trigger",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToTriggers) {
                                Text("Verwalten")
                            }
                        }

                        if (user?.triggers.isNullOrEmpty()) {
                            Text(
                                text = "Noch keine Trigger hinterlegt",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            user?.triggers?.forEach { trigger ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = trigger,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Sober Days Dialog
    if (showSoberDaysDialog) {
        SoberDaysDialog(
            onDismiss = { showSoberDaysDialog = false },
            onSetDate = { date ->
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val timestamp = com.google.firebase.Timestamp(calendar.time)
                viewModel.updateSoberSince(timestamp)
                showSoberDaysDialog = false
            },
            onMarkAsDrunk = {
                viewModel.markAsDrunk()
                showSoberDaysDialog = false
            },
            currentSoberSince = user?.soberSince?.toDate()
        )
    }
}

@Composable
fun MoodCalendarView(
    moodEntries: List<MoodEntry>,
    onDateClick: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Get first day of month and number of days
    calendar.set(currentYear, currentMonth, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Create a map of date to mood entry
    val moodMap = moodEntries.associateBy { entry ->
        entry.date?.toDate()?.let { date ->
            val cal = Calendar.getInstance()
            cal.time = date
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    Column {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        var dayCounter = 1
        while (dayCounter <= daysInMonth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 1..7) {
                    if (dayCounter == 1 && dayOfWeek < firstDayOfWeek) {
                        // Empty cell before first day
                        Spacer(modifier = Modifier.weight(1f))
                    } else if (dayCounter <= daysInMonth) {
                        val day = dayCounter++
                        val moodEntry = moodMap[Triple(currentYear, currentMonth, day)]
                        val moodColor = moodEntry?.getMoodColor() ?: null

                        val color = when (moodColor) {
                            MoodColor.RED -> Color(0xFFFF5252)
                            MoodColor.YELLOW -> Color(0xFFFFEB3B)
                            MoodColor.GREEN -> Color(0xFF4CAF50)
                            null -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = if (moodColor != null) 0.6f else 0.1f))
                                .clickable {
                                    calendar.set(currentYear, currentMonth, day)
                                    onDateClick(calendar.time)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (moodColor != null) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Legend
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(Color(0xFFFF5252), "Kritisch (1-3)")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(Color(0xFFFFEB3B), "Neutral (4-7)")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(Color(0xFF4CAF50), "Stabil (8-10)")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoberDaysDialog(
    onDismiss: () -> Unit,
    onSetDate: (Date) -> Unit,
    onMarkAsDrunk: () -> Unit,
    currentSoberSince: Date? = null
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { 
        mutableStateOf(currentSoberSince ?: Date())
    }
    
    val datePicker = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                showDatePicker = false
                onSetDate(selectedDate)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nüchterne Tage einstellen")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Wähle das Datum, ab dem du nüchtern bist. Die nüchternen Tage werden automatisch berechnet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (currentSoberSince != null) {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Aktuelles Datum:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = dateFormat.format(currentSoberSince),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Divider()
                
                // Button for "Ich habe Getrunken"
                OutlinedButton(
                    onClick = {
                        onMarkAsDrunk()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ich habe Getrunken")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showDatePicker = true
                }
            ) {
                Text("Datum setzen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Abbrechen")
            }
        }
    )
    
    // Show date picker when button is clicked
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            datePicker.show()
        }
    }
}

// Factory for ViewModel
class PatientDashboardViewModelFactory(private val userId: String) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientDashboardViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

