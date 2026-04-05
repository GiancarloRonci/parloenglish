package com.example.parloenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parloenglish.auth.FirebaseAuthRepository
import com.example.parloenglish.auth.model.AuthState
import com.example.parloenglish.auth.ui.AuthScreen
import com.example.parloenglish.auth.ui.AuthViewModel
import com.example.parloenglish.auth.ui.AuthViewModelFactory
import com.example.parloenglish.repository.VocabularyRepository
import com.example.parloenglish.ui.*
import com.example.parloenglish.ui.theme.ParloEnglishTheme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authRepository = FirebaseAuthRepository()
        val vocabularyRepository = VocabularyRepository()

        lifecycleScope.launch {
            vocabularyRepository.seedInitialData()
        }

        enableEdgeToEdge()
        setContent {
            ParloEnglishTheme {
                val navController = rememberNavController()
                
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authRepository)
                )
                
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(vocabularyRepository)
                )
                
                val authState by authViewModel.authState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "welcome"
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onStartClick = { 
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
                            homeViewModel = homeViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onExitApp = {
                                finishAffinity()
                                exitProcess(0)
                            },
                            onStudyClick = { source, level, categories, direction ->
                                val catsParam = if (categories.isEmpty()) "NONE" else categories.joinToString(",")
                                navController.navigate("study/$source/$level/$catsParam/$direction")
                            },
                            onResetProgress = {
                                userSession?.let {
                                    lifecycleScope.launch {
                                        vocabularyRepository.resetUserProgress(it.userId)
                                    }
                                }
                            },
                            onDebugClick = {
                                navController.navigate("debug")
                            }
                        )
                    }

                    composable(
                        route = "study/{sourceType}/{level}/{categories}/{direction}",
                        arguments = listOf(
                            navArgument("sourceType") { type = NavType.StringType },
                            navArgument("level") { type = NavType.StringType },
                            navArgument("categories") { type = NavType.StringType },
                            navArgument("direction") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val sourceType = backStackEntry.arguments?.getString("sourceType")
                        val level = backStackEntry.arguments?.getString("level") ?: "A1"
                        val categoriesStr = backStackEntry.arguments?.getString("categories")
                        val direction = backStackEntry.arguments?.getString("direction") ?: "IT_TO_EN"
                        
                        val categoriesList = if (categoriesStr == null || categoriesStr == "NONE") {
                            null
                        } else {
                            categoriesStr.split(",")
                        }

                        val userSession = (authState as? AuthState.Authenticated)?.userSession
                            ?: authRepository.getCurrentUser()
                        
                        if (userSession != null) {
                            val studyViewModel: StudyViewModel = viewModel(
                                factory = StudyViewModelFactory(
                                    vocabularyRepository, 
                                    userSession.userId,
                                    if (sourceType == "ALL") null else sourceType,
                                    level,
                                    categoriesList,
                                    direction
                                )
                            )
                            StudyScreen(
                                viewModel = studyViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("debug") {
                        val userSession = (authState as? AuthState.Authenticated)?.userSession
                            ?: authRepository.getCurrentUser()
                        
                        if (userSession != null) {
                            val debugViewModel: DebugViewModel = viewModel(
                                factory = DebugViewModelFactory(vocabularyRepository, userSession.userId)
                            )
                            DebugScreen(
                                viewModel = debugViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
