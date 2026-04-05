package com.example.parloenglish.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DebugViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentDirection by viewModel.currentDirection.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Database") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Selettore direzione nella pagina di debug
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentDirection == "IT_TO_EN",
                    onClick = { viewModel.setDirection("IT_TO_EN") },
                    label = { Text("IT → EN") }
                )
                FilterChip(
                    selected = currentDirection == "EN_TO_IT",
                    onClick = { viewModel.setDirection("EN_TO_IT") },
                    label = { Text("EN → IT") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is DebugState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is DebugState.Error -> Text("Errore: ${state.message}", modifier = Modifier.padding(16.dp))
                    is DebugState.Success -> {
                        if (state.items.isEmpty()) {
                            Text("Nessun dato trovato.", modifier = Modifier.padding(16.dp))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.items) { (vocab, progress) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "${vocab.italian} -> ${vocab.english}", fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                if (progress != null) {
                                                    val nextReviewDate = progress.nextReview?.toDate()
                                                    val nextReviewStr = if (nextReviewDate != null) dateFormat.format(nextReviewDate) else "N/A"
                                                    
                                                    Text(text = "Prossimo ripasso: $nextReviewStr", color = MaterialTheme.colorScheme.primary)
                                                    Text(text = "Intervallo: ${progress.intervalDays} giorni", style = MaterialTheme.typography.bodySmall)
                                                } else {
                                                    Text(text = "Stato: MAI STUDIATA", color = MaterialTheme.colorScheme.error)
                                                }
                                            }

                                            if (progress != null) {
                                                IconButton(onClick = { viewModel.resetCardProgress(progress.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Reset Carta",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
