package com.example.parloenglish.auth.model

sealed class AuthState {

    object Loading : AuthState()

    object Unauthenticated : AuthState()

    data class Authenticated(
        val userSession: UserSession
    ) : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}