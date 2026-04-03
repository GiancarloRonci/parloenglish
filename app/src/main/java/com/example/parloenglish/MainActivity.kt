package com.example.parloenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parloenglish.auth.FirebaseAuthRepository
import com.example.parloenglish.auth.model.AuthState
import com.example.parloenglish.auth.ui.AuthScreen
import com.example.parloenglish.auth.ui.AuthViewModel
import com.example.parloenglish.auth.ui.AuthViewModelFactory
import com.example.parloenglish.ui.HomeScreen
import com.example.parloenglish.ui.WelcomeScreen
import com.example.parloenglish.ui.theme.ParloEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authRepository = FirebaseAuthRepository()

        enableEdgeToEdge()
        setContent {
            ParloEnglishTheme {
                val navController = rememberNavController()
                
                // Usiamo un Factory per passare il repository al ViewModel
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authRepository)
                )
                
                val authState by authViewModel.authState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "welcome"
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onStartClick = { 
                                // Se l'utente è già loggato, vai alla Home, altrimenti Auth
                                if (authRepository.getCurrentUser() != null) {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("auth")
                                }
                            }
                        )
                    }
                    
                    composable("auth") {
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { 
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("home") {
                        val userSession = (authState as? AuthState.Authenticated)?.userSession
                            ?: authRepository.getCurrentUser()
                            
                        HomeScreen(
                            userSession = userSession,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
