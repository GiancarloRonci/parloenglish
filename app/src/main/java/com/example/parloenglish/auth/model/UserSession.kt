package com.example.parloenglish.auth.model

data class UserSession(
    val userId: String,
    val email: String?,
    val displayName: String?
)
