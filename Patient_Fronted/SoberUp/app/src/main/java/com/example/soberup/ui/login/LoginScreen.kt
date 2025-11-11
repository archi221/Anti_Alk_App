package com.example.soberup.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soberup.data.AuthRepository
import com.example.soberup.data.User
import kotlinx.coroutines.launch

/**
 * Login screen composable
 * @param onLoginSuccess Callback when login is successful, provides the authenticated User
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "SoberUp",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Login",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name TextField (für Debug: Login mit Name)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Error Message
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Login Button
        Button(
            onClick = {
                if (name.isBlank() || password.isBlank()) {
                    errorMessage = "Bitte Name und Passwort eingeben"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                scope.launch {
                    try {
                        val user = authRepository.loginByName(name.trim(), password)
                        if (user != null) {
                            isLoading = false
                            onLoginSuccess(user)
                        } else {
                            errorMessage = "Ungültiger Name oder Passwort"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Login fehlgeschlagen: ${e.message}"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

