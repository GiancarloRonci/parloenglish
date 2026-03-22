package com.example.parloenglish.auth

import com.example.parloenglish.auth.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun getCurrentUser(): UserSession? {
        return firebaseAuth.currentUser?.let { user ->
            UserSession(
                userId = user.uid,
                email = user.email,
                displayName = user.displayName
            )
        }
    }

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(
                    UserSession(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                )
            } else {
                Result.failure(Exception("Errore durante il login: utente nullo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): Result<UserSession> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Aggiorniamo il profilo con il nome visualizzato se fornito
                if (displayName != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                
                Result.success(
                    UserSession(
                        userId = user.uid,
                        email = user.email,
                        displayName = displayName ?: user.displayName
                    )
                )
            } else {
                Result.failure(Exception("Errore durante la registrazione: utente nullo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
