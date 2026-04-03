package com.example.parloenglish.model

import com.google.firebase.Timestamp

/**
 * Represents the progress of a specific user on a vocabulary item.
 * Used for the Spaced Repetition System (SRS) logic.
 */
data class UserProgress(
    val id: String = "", // Firestore document ID
    val vocabularyId: String = "",
    val userId: String = "",
    val lastReview: Timestamp? = null,
    val nextReview: Timestamp? = null,
    val intervalDays: Int = 0 // Current interval in days for SRS logic
)
