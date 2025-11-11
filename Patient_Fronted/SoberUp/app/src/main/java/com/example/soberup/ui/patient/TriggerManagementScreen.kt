package com.example.soberup.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerManagementScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: TriggerManagementViewModel = viewModel(factory = TriggerManagementViewModelFactory(userId))
) {
    val triggers by viewModel.triggers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var newTriggerText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trigger verwalten") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Trigger hinzufügen")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Trigger hinzufügen")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

            // Success message
            successMessage?.let { success ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = success,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Info card
            item {
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
                            text = "Was sind Trigger?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Trigger sind Situationen oder Umstände, die das Risiko eines Rückfalls erhöhen können. Beispiele: Stress auf Arbeit, Geselligkeit mit Alkohol, emotionale Belastungen.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Triggers list
            if (triggers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Noch keine Trigger",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Füge deine ersten Trigger hinzu",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(triggers) { trigger ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = trigger,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteTrigger(trigger)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Trigger Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Neuen Trigger hinzufügen") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTriggerText,
                        onValueChange = { newTriggerText = it },
                        label = { Text("Trigger") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("z.B. Stress auf Arbeit") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTriggerText.isNotBlank()) {
                            viewModel.addTrigger(newTriggerText.trim())
                            newTriggerText = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newTriggerText.isNotBlank() && !isLoading
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

class TriggerManagementViewModel(
    private val userId: String,
    private val userRepository: com.example.soberup.data.UserRepository = com.example.soberup.data.UserRepository()
) : androidx.lifecycle.ViewModel() {
    private val _triggers = MutableStateFlow<List<String>>(emptyList())
    val triggers: StateFlow<List<String>> = _triggers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadTriggers()
    }

    private fun loadTriggers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(userId)
                _triggers.value = user?.triggers ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Laden der Trigger: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTrigger(trigger: String) {
        if (trigger.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val currentTriggers = _triggers.value.toMutableList()
                if (!currentTriggers.contains(trigger)) {
                    currentTriggers.add(trigger)
                    val success = userRepository.updateTriggers(userId, currentTriggers)
                    if (success) {
                        _triggers.value = currentTriggers
                        _successMessage.value = "Trigger hinzugefügt"
                    } else {
                        _errorMessage.value = "Fehler beim Hinzufügen des Triggers"
                    }
                } else {
                    _errorMessage.value = "Dieser Trigger existiert bereits"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTrigger(trigger: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val currentTriggers = _triggers.value.toMutableList()
                currentTriggers.remove(trigger)
                val success = userRepository.updateTriggers(userId, currentTriggers)
                if (success) {
                    _triggers.value = currentTriggers
                    _successMessage.value = "Trigger gelöscht"
                } else {
                    _errorMessage.value = "Fehler beim Löschen des Triggers"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class TriggerManagementViewModelFactory(private val userId: String) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TriggerManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TriggerManagementViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

