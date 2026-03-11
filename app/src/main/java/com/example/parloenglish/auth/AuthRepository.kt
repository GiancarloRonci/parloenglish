package com.example.parloenglish.auth

import com.example.parloenglish.auth.model.UserSession

interface AuthRepository {

    fun getCurrentUser(): UserSession?

    suspend fun login(
        email: String,
        password: String
    ): Result<UserSession>

    suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): Result<UserSession>

    suspend fun logout()

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit>
}