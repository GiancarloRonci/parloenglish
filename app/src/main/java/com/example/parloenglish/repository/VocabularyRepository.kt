package com.example.parloenglish.repository

import android.util.Log
import com.example.parloenglish.model.UserProgress
import com.example.parloenglish.model.VocabularyItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class VocabularyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "VocabularyRepo"
    private val vocabularyCollection = firestore.collection("vocabulary")
    private val progressCollection = firestore.collection("user_progress")

    private val initialItems = listOf(
        VocabularyItem(italian = "Ciao", english = "Hello", level = "A1"),
        VocabularyItem(italian = "Grazie", english = "Thank you", level = "A1"),
        VocabularyItem(italian = "Per favore", english = "Please", level = "A1"),
        VocabularyItem(italian = "Sì", english = "Yes", level = "A1"),
        VocabularyItem(italian = "No", english = "No", level = "A1"),
        VocabularyItem(italian = "Mi chiamo...", english = "My name is...", level = "A1"),
        VocabularyItem(italian = "Come stai?", english = "How are you?", level = "A1"),
        VocabularyItem(italian = "Piacere di conoscerti", english = "Nice to meet you", level = "A1"),
        VocabularyItem(italian = "Scusa", english = "Excuse me", level = "A1"),
        VocabularyItem(italian = "Arrivederci", english = "Goodbye", level = "A1")
    )

    suspend fun getVocabularyByLevel(level: String): Result<List<VocabularyItem>> {
        return try {
            val snapshot = vocabularyCollection
                .whereEqualTo("level", level)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VocabularyItem::class.java)?.copy(id = doc.id)
            }
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error getVocabularyByLevel: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllVocabularyWithProgress(userId: String): Result<List<Pair<VocabularyItem, UserProgress?>>> {
        return try {
            val allVocab = vocabularyCollection.get().await().documents.mapNotNull { doc ->
                doc.toObject(VocabularyItem::class.java)?.copy(id = doc.id)
            }
            
            val progressSnapshot = progressCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val userProgressMap = progressSnapshot.documents.associate { doc ->
                val progress = doc.toObject(UserProgress::class.java)!!.copy(id = doc.id)
                progress.vocabularyId to progress
            }

            val result = allVocab.map { item ->
                item to userProgressMap[item.id]
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDueCards(userId: String, level: String): Result<List<Pair<VocabularyItem, UserProgress?>>> {
        return try {
            Log.d(TAG, "Fetching cards for user: $userId, level: $level")
            
            val allVocab = getVocabularyByLevel(level).getOrThrow()
            
            if (allVocab.size < initialItems.size) {
                seedInitialData()
                return getDueCards(userId, level)
            }

            val progressSnapshot = progressCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val userProgressMap = progressSnapshot.documents.associate { doc ->
                val progress = doc.toObject(UserProgress::class.java)!!.copy(id = doc.id)
                progress.vocabularyId to progress
            }

            val now = Timestamp.now()
            
            val dueCards = allVocab.mapNotNull { item ->
                val progress = userProgressMap[item.id]
                if (progress == null || progress.nextReview == null || progress.nextReview <= now) {
                    item to progress
                } else {
                    null
                }
            }
            
            Log.d(TAG, "Due cards after filter: ${dueCards.size}")
            Result.success(dueCards)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getDueCards: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateCardProgress(userId: String, vocabularyId: String, currentProgress: UserProgress?, days: Int): Result<Unit> {
        return try {
            val now = Date()
            val calendar = Calendar.getInstance()
            calendar.time = now

            if (days == -1) {
                calendar.add(Calendar.YEAR, 100)
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, days)
            }
            
            val nextReviewDate = calendar.time

            val updatedProgress = UserProgress(
                vocabularyId = vocabularyId,
                userId = userId,
                lastReview = Timestamp(now),
                nextReview = Timestamp(nextReviewDate),
                intervalDays = days
            )

            if (currentProgress?.id != null && currentProgress.id.isNotEmpty()) {
                progressCollection.document(currentProgress.id).set(updatedProgress).await()
            } else {
                progressCollection.add(updatedProgress).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetUserProgress(userId: String): Result<Unit> {
        return try {
            val snapshot = progressCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "Progress reset successfully for $userId")
            seedInitialData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetSingleCardProgress(progressId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Attempting to delete progress document ID: $progressId")
            if (progressId.isEmpty()) {
                Log.e(TAG, "Error: progressId is empty")
                return Result.failure(Exception("ID progresso vuoto"))
            }
            
            progressCollection.document(progressId).delete().await()
            Log.d(TAG, "Deletion successful for ID: $progressId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting single card progress: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun seedInitialData() {
        try {
            val snapshot = vocabularyCollection.get().await()
            val existingItalians = snapshot.documents.mapNotNull { it.getString("italian") }.toSet()

            for (item in initialItems) {
                if (!existingItalians.contains(item.italian)) {
                    Log.d(TAG, "Adding missing word: ${item.italian}")
                    vocabularyCollection.add(item).await()
                }
            }
            Log.d(TAG, "Database seed check completed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during seed: ${e.message}")
        }
    }
}
