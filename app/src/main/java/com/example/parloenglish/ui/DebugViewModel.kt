package com.example.parloenglish.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parloenglish.model.UserProgress
import com.example.parloenglish.model.VocabularyItem
import com.example.parloenglish.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DebugState {
    object Loading : DebugState()
    data class Success(val items: List<Pair<VocabularyItem, UserProgress?>>) : DebugState()
    data class Error(val message: String) : DebugState()
}

class DebugViewModel(
    private val repository: VocabularyRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebugState>(DebugState.Loading)
    val uiState: StateFlow<DebugState> = _uiState.asStateFlow()

    private val _currentDirection = MutableStateFlow("IT_TO_EN")
    val currentDirection: StateFlow<String> = _currentDirection.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        refreshData()
    }

    fun setDirection(direction: String) {
        _currentDirection.value = direction
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = DebugState.Loading
            val result = repository.getAllVocabularyWithProgress(userId, _currentDirection.value)
            result.onSuccess { items ->
                _uiState.value = DebugState.Success(items)
            }.onFailure { e ->
                _uiState.value = DebugState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetCardProgress(progressId: String) {
        viewModelScope.launch {
            val result = repository.resetSingleCardProgress(progressId)
            result.onSuccess {
                _toastMessage.emit("Reset effettuato con successo")
                refreshData()
            }.onFailure { e ->
                _toastMessage.emit("Errore: ${e.message}")
            }
        }
    }
}

class DebugViewModelFactory(
    private val repository: VocabularyRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DebugViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DebugViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
