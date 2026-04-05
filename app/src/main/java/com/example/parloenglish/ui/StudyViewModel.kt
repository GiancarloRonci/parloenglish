package com.example.parloenglish.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parloenglish.model.UserProgress
import com.example.parloenglish.model.VocabularyItem
import com.example.parloenglish.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StudyState {
    object Loading : StudyState()
    data class Success(val cards: List<Pair<VocabularyItem, UserProgress?>>) : StudyState()
    data class Error(val message: String) : StudyState()
    object Empty : StudyState()
}

class StudyViewModel(
    private val repository: VocabularyRepository,
    private val userId: String,
    private val sourceType: String? = null,
    private val level: String = "A1",
    private val categories: List<String>? = null,
    private val studyDirection: String = "IT_TO_EN"
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyState>(StudyState.Loading)
    val uiState: StateFlow<StudyState> = _uiState.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isRevealed = MutableStateFlow(false)
    val isRevealed: StateFlow<Boolean> = _isRevealed.asStateFlow()

    init {
        loadDueCards()
    }

    fun loadDueCards() {
        viewModelScope.launch {
            _uiState.value = StudyState.Loading
            val result = repository.getDueCards(userId, level, sourceType, categories, studyDirection)
            result.onSuccess { cards ->
                if (cards.isEmpty()) {
                    _uiState.value = StudyState.Empty
                } else {
                    _uiState.value = StudyState.Success(cards)
                    _currentIndex.value = 0
                    _isRevealed.value = false
                }
            }.onFailure { e ->
                _uiState.value = StudyState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun revealTranslation() {
        _isRevealed.value = true
    }

    fun markAsLearnedWithInterval(days: Int) {
        val state = _uiState.value
        if (state is StudyState.Success) {
            val currentPair = state.cards[_currentIndex.value]
            viewModelScope.launch {
                repository.updateCardProgress(
                    userId = userId,
                    vocabularyId = currentPair.first.id,
                    currentProgress = currentPair.second,
                    days = days,
                    studyDirection = studyDirection
                )
                
                if (_currentIndex.value < state.cards.size - 1) {
                    _currentIndex.value += 1
                    _isRevealed.value = false
                } else {
                    _uiState.value = StudyState.Empty
                }
            }
        }
    }
    
    fun getStudyDirection() = studyDirection
}

class StudyViewModelFactory(
    private val repository: VocabularyRepository,
    private val userId: String,
    private val sourceType: String? = null,
    private val level: String = "A1",
    private val categories: List<String>? = null,
    private val studyDirection: String = "IT_TO_EN"
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository, userId, sourceType, level, categories, studyDirection) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
