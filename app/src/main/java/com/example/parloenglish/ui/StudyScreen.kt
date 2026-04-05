package com.example.parloenglish.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isRevealed by viewModel.isRevealed.collectAsState()
    val direction = viewModel.getStudyDirection()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studio Carte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is StudyState.Loading -> CircularProgressIndicator()
                is StudyState.Error -> Text("Errore: ${state.message}")
                is StudyState.Empty -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ottimo lavoro!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Non ci sono altre carte da ripassare per ora.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Torna alla Home") }
                    }
                }
                is StudyState.Success -> {
                    val currentPair = state.cards[currentIndex]
                    val vocab = currentPair.first

                    // Determiniamo cosa mostrare sul fronte e sul retro
                    val frontText = if (direction == "IT_TO_EN") vocab.italian else vocab.english
                    val backText = if (direction == "IT_TO_EN") vocab.english else vocab.italian

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Carta ${currentIndex + 1} di ${state.cards.size} (${if (direction == "IT_TO_EN") "IT → EN" else "EN → IT"})",
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = frontText,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    
                                    if (isRevealed) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                        Text(
                                            text = backText,
                                            fontSize = 28.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        if (!isRevealed) {
                            Button(
                                onClick = { viewModel.revealTranslation() },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Gira Carta", fontSize = 18.sp)
                            }
                        } else {
                            Text(
                                text = "Quando vuoi ripassarla?",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntervalButton("Domani", 1, Modifier.weight(1f)) { viewModel.markAsLearnedWithInterval(1) }
                                    IntervalButton("Settimana", 7, Modifier.weight(1f)) { viewModel.markAsLearnedWithInterval(7) }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntervalButton("Mese", 30, Modifier.weight(1f)) { viewModel.markAsLearnedWithInterval(30) }
                                    IntervalButton("Anno", 365, Modifier.weight(1f)) { viewModel.markAsLearnedWithInterval(365) }
                                }
                                Button(
                                    onClick = { viewModel.markAsLearnedWithInterval(-1) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Mai più")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntervalButton(label: String, days: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = label, textAlign = TextAlign.Center)
    }
}
