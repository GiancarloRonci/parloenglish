package com.example.parloenglish.auth

import android.util.Log
import com.example.parloenglish.auth.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

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
            Log.d(TAG, "Tentativo di login per: $email")
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Login successo: ${user.uid}")
                Result.success(
                    UserSession(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                )
            } else {
                Log.e(TAG, "Login fallito: utente nullo")
                Result.failure(Exception("Errore durante il login: utente nullo"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore login: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): Result<UserSession> {
        return try {
            Log.d(TAG, "Tentativo di registrazione per: $email")
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                if (displayName != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                Log.d(TAG, "Registrazione successo: ${user.uid}")
                Result.success(
                    UserSession(
                        userId = user.uid,
                        email = user.email,
                        displayName = displayName ?: user.displayName
                    )
                )
            } else {
                Log.e(TAG, "Registrazione fallita: utente nullo")
                Result.failure(Exception("Errore durante la registrazione: utente nullo"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore registrazione: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        Log.d(TAG, "Logout effettuato")
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "Tentativo invio reset password a: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Richiesta reset inviata con successo a Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Errore invio reset password: ${e.message}", e)
            Result.failure(e)
        }
    }
}
