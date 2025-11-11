package com.example.soberup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.soberup.data.SessionManager
import com.example.soberup.data.User
import com.example.soberup.navigation.NavGraph
import com.example.soberup.navigation.Screen
import com.example.soberup.ui.theme.SoberUpTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        enableEdgeToEdge()
        setContent {
            SoberUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()

                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        onLoginSuccess = { user ->
                            // Save session
                            coroutineScope.launch {
                                sessionManager.saveSession(user.id, user.username, user.role)
                            }
                        },
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}