package com.example.parloenglish.model

import com.google.firebase.Timestamp

/**
 * Represents the progress of a specific user on a vocabulary item for a specific direction.
 * @property studyDirection Identifies the mode of study: "IT_TO_EN", "EN_TO_IT", or future "SPOKEN_EN_TO_IT"
 */
data class UserProgress(
    val id: String = "", 
    val vocabularyId: String = "",
    val userId: String = "",
    val lastReview: Timestamp? = null,
    val nextReview: Timestamp? = null,
    val intervalDays: Int = 0,
    val studyDirection: String = "IT_TO_EN"
)
