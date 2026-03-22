package com.example.parloenglish.auth

import com.example.parloenglish.auth.model.UserSession
import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {
    private var currentUser: UserSession? = null

    override fun getCurrentUser(): UserSession? = currentUser

    override suspend fun login(email: String, password: String): Result<UserSession> {
        delay(2000) // Simula latenza di rete
        return if (email == "test@test.com" && password == "password") {
            val session = UserSession("1", email, "Utente Test")
            currentUser = session
            Result.success(session)
        } else {
            Result.failure(Exception("Credenziali errate (usa test@test.com / password)"))
        }
    }

    override suspend fun register(email: String, password: String, displayName: String?): Result<UserSession> {
        delay(2000)
        val session = UserSession("2", email, displayName ?: "Nuovo Utente")
        currentUser = session
        return Result.success(session)
    }

    override suspend fun logout() {
        delay(500)
        currentUser = null
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }
}
