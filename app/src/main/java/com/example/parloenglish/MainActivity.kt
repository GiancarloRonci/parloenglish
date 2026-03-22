package com.example.parloenglish

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.parloenglish.auth.FirebaseAuthRepository
import com.example.parloenglish.auth.ui.AuthScreen
import com.example.parloenglish.auth.ui.AuthViewModel
import com.example.parloenglish.ui.WelcomeScreen
import com.example.parloenglish.ui.theme.ParloEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inizializziamo il Repository reale e il ViewModel
        // Nota: In un'app reale useremmo la Dependency Injection (es. Hilt)
        val authRepository = FirebaseAuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        enableEdgeToEdge()
        setContent {
            ParloEnglishTheme {
                var currentScreen by remember { mutableStateOf("welcome") }
                val context = LocalContext.current

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "welcome" -> WelcomeScreen(
                            onStartClick = { currentScreen = "auth" }
                        )
                        "auth" -> AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { 
                                Toast.makeText(context, "Login effettuato!", Toast.LENGTH_SHORT).show()
                                // Qui potremmo cambiare schermata verso la "Home"
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
