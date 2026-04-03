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

/**
 * States representing the UI for the study session.
 */
sealed class StudyState {
    object Loading : StudyState()
    data class Success(val cards: List<Pair<VocabularyItem, UserProgress?>>) : StudyState()
    data class Error(val message: String) : StudyState()
    object Empty : StudyState()
}

/**
 * ViewModel responsible for managing the study session logic.
 */
class StudyViewModel(
    private val repository: VocabularyRepository,
    private val userId: String
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

    /**
     * Fetches cards that are due for review from the repository.
     */
    fun loadDueCards() {
        viewModelScope.launch {
            _uiState.value = StudyState.Loading
            // For now, we only load A1 level cards
            val result = repository.getDueCards(userId, "A1")
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

    /**
     * Reveals the English translation of the current card.
     */
    fun revealTranslation() {
        _isRevealed.value = true
    }

    /**
     * Updates the progress for the current card and moves to the next eligible one.
     * @param days The interval in days selected by the user.
     */
    fun markAsLearnedWithInterval(days: Int) {
        val state = _uiState.value
        if (state is StudyState.Success) {
            val currentPair = state.cards[_currentIndex.value]
            viewModelScope.launch {
                // Update progress in the database
                repository.updateCardProgress(
                    userId = userId,
                    vocabularyId = currentPair.first.id,
                    currentProgress = currentPair.second,
                    days = days
                )
                
                // Move to the next card in the current session list
                if (_currentIndex.value < state.cards.size - 1) {
                    _currentIndex.value += 1
                    _isRevealed.value = false // Hide the answer for the new card
                } else {
                    // Session finished, no more due cards in this batch
                    _uiState.value = StudyState.Empty
                }
            }
        }
    }
}

/**
 * Factory to create StudyViewModel with custom parameters.
 */
class StudyViewModelFactory(
    private val repository: VocabularyRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
