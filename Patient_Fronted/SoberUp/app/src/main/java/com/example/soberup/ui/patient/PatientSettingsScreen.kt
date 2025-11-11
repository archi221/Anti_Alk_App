package com.example.soberup.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.soberup.data.SOSContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSettingsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: PatientSettingsViewModel = viewModel(factory = PatientSettingsViewModelFactory(userId))
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var sosContactName by remember { mutableStateOf(user?.sosContact?.name ?: "") }
    var sosContactPhone by remember { mutableStateOf(user?.sosContact?.phone ?: "") }

    LaunchedEffect(user) {
        sosContactName = user?.sosContact?.name ?: ""
        sosContactPhone = user?.sosContact?.phone ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
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

            // SOS Contact Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SOS-Kontakt",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Im Notfall wird dieser Kontakt automatisch benachrichtigt, wenn du zwei aufeinanderfolgende Tage mit schlechter Stimmung (1-3) hast.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = sosContactName,
                            onValueChange = { sosContactName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = sosContactPhone,
                            onValueChange = { sosContactPhone = it },
                            label = { Text("Telefonnummer") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            placeholder = { Text("+49 123 456789") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (sosContactName.isNotBlank() && sosContactPhone.isNotBlank()) {
                                    viewModel.updateSOSContact(
                                        SOSContact(
                                            name = sosContactName.trim(),
                                            phone = sosContactPhone.trim()
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && sosContactName.isNotBlank() && sosContactPhone.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("SOS-Kontakt speichern")
                            }
                        }
                    }
                }
            }
        }
    }
}

class PatientSettingsViewModel(
    private val userId: String,
    private val userRepository: com.example.soberup.data.UserRepository = com.example.soberup.data.UserRepository()
) : androidx.lifecycle.ViewModel() {
    private val _user = MutableStateFlow<com.example.soberup.data.User?>(null)
    val user: StateFlow<com.example.soberup.data.User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userData = userRepository.getUser(userId)
                _user.value = userData
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Laden der Daten: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSOSContact(sosContact: SOSContact) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val success = userRepository.updateSOSContact(userId, sosContact)
                if (success) {
                    _successMessage.value = "SOS-Kontakt erfolgreich gespeichert"
                    loadUser() // Reload user data
                } else {
                    _errorMessage.value = "Fehler beim Speichern des SOS-Kontakts"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class PatientSettingsViewModelFactory(private val userId: String) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientSettingsViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

