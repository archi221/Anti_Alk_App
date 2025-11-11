package com.example.soberup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.soberup.data.SessionManager
import com.example.soberup.data.User
import com.example.soberup.ui.login.LoginScreen
import com.example.soberup.ui.patient.PatientDashboardScreen
import com.example.soberup.ui.patient.PatientSettingsScreen
import com.example.soberup.ui.patient.TriggerManagementScreen
import com.example.soberup.ui.patient.SupportLocationsScreen
import kotlinx.coroutines.flow.first

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PatientDashboard : Screen("patient_dashboard")
    object PatientProfile : Screen("patient_profile")
    object PatientSettings : Screen("patient_settings")
    object SupportLocations : Screen("support_locations")
    object TriggerManagement : Screen("trigger_management")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    onLoginSuccess: (User) -> Unit,
    sessionManager: SessionManager? = null
) {
    var currentUserId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUserId = user.id
                    onLoginSuccess(user)
                    // Navigate based on role
                    when (user.role) {
                        "patient" -> navController.navigate(Screen.PatientDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "doctor" -> {
                            // TODO: Navigate to doctor dashboard
                            navController.navigate(Screen.PatientDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "admin" -> {
                            // TODO: Navigate to admin dashboard
                            navController.navigate(Screen.PatientDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        else -> {
                            // Stay on login
                        }
                    }
                }
            )
        }

        composable(Screen.PatientDashboard.route) {
            var userId by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(Unit) {
                if (currentUserId != null) {
                    userId = currentUserId
                } else {
                    sessionManager?.userId?.first()?.let {
                        userId = it
                    }
                }
            }
            
            if (userId != null) {
                PatientDashboardScreen(
                    userId = userId!!,
                    onNavigateToProfile = {
                        navController.navigate(Screen.PatientProfile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.PatientSettings.route)
                    },
                    onNavigateToSupportLocations = {
                        navController.navigate(Screen.SupportLocations.route)
                    },
                    onNavigateToTriggers = {
                        navController.navigate(Screen.TriggerManagement.route)
                    }
                )
            }
        }

        composable(Screen.PatientProfile.route) {
            // TODO: Implement PatientProfileScreen
            var profileUserId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                if (currentUserId != null) {
                    profileUserId = currentUserId
                } else {
                    sessionManager?.userId?.first()?.let {
                        profileUserId = it
                    }
                }
            }
            if (profileUserId != null) {
                PatientDashboardScreen(
                    userId = profileUserId!!,
                    onNavigateToProfile = { },
                    onNavigateToSettings = { navController.navigate(Screen.PatientSettings.route) },
                    onNavigateToSupportLocations = { navController.navigate(Screen.SupportLocations.route) },
                    onNavigateToTriggers = { navController.navigate(Screen.TriggerManagement.route) }
                )
            }
        }

        composable(Screen.PatientSettings.route) {
            var settingsUserId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                if (currentUserId != null) {
                    settingsUserId = currentUserId
                } else {
                    sessionManager?.userId?.first()?.let {
                        settingsUserId = it
                    }
                }
            }
            // Show settings screen even if userId is not yet loaded (will show loading state)
            PatientSettingsScreen(
                userId = settingsUserId ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SupportLocations.route) {
            SupportLocationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TriggerManagement.route) {
            var triggerUserId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                if (currentUserId != null) {
                    triggerUserId = currentUserId
                } else {
                    sessionManager?.userId?.first()?.let {
                        triggerUserId = it
                    }
                }
            }
            if (triggerUserId != null) {
                TriggerManagementScreen(
                    userId = triggerUserId!!,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

