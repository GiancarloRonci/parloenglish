package com.example.parloenglish.model

/**
 * Represents a word or phrase in the vocabulary.
 * @property id Firestore document ID
 * @property italian The Italian word/phrase
 * @property english The English translation
 * @property level Difficulty level (e.g., "A1")
 * @property category Category (e.g., "General", "Travel")
 * @property sourceType Indicates if the item is "DEFAULT" (system) or "CUSTOM" (user-added)
 */
data class VocabularyItem(
    val id: String = "",
    val italian: String = "",
    val english: String = "",
    val level: String = "A1",
    val category: String = "General",
    val sourceType: String = "DEFAULT" // "DEFAULT" or "CUSTOM"
)
