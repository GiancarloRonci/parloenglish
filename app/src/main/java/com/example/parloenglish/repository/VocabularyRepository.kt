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
        VocabularyItem(italian = "Ciao", english = "Hello", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Grazie", english = "Thank you", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Per favore", english = "Please", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Sì", english = "Yes", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "No", english = "No", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Mi chiamo...", english = "My name is...", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Introductions")),
        VocabularyItem(italian = "Come stai?", english = "How are you?", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Piacere di conoscerti", english = "Nice to meet you", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Introductions")),
        VocabularyItem(italian = "Scusa", english = "Excuse me", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Arrivederci", english = "Goodbye", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings"))
    )

    suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val snapshot = vocabularyCollection.get().await()
            val categories = snapshot.documents
                .flatMap { (it.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList() }
                .distinct()
                .filter { it.isNotEmpty() }
                .sorted()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVocabularyFiltered(level: String, sourceType: String?, categories: List<String>?): Result<List<VocabularyItem>> {
        return try {
            var query = vocabularyCollection.whereEqualTo("level", level)
            if (sourceType != null && sourceType != "ALL") {
                query = query.whereEqualTo("sourceType", sourceType)
            }
            
            val snapshot = query.get().await()
            var items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VocabularyItem::class.java)?.copy(id = doc.id)
            }
            
            if (!categories.isNullOrEmpty()) {
                items = items.filter { item ->
                    item.categories.any { it in categories }
                }
            }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllVocabularyWithProgress(userId: String, studyDirection: String): Result<List<Pair<VocabularyItem, UserProgress?>>> {
        return try {
            val allVocab = vocabularyCollection.get().await().documents.mapNotNull { doc ->
                doc.toObject(VocabularyItem::class.java)?.copy(id = doc.id)
            }
            
            val progressSnapshot = progressCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("studyDirection", studyDirection)
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

    suspend fun getDueCards(
        userId: String, 
        level: String, 
        sourceType: String? = null,
        categories: List<String>? = null,
        studyDirection: String = "IT_TO_EN"
    ): Result<List<Pair<VocabularyItem, UserProgress?>>> {
        return try {
            val allVocab = getVocabularyFiltered(level, sourceType, categories).getOrThrow()
            
            if (allVocab.isEmpty() && (sourceType == "DEFAULT" || sourceType == "ALL" || sourceType == null)) {
                seedInitialData()
                return getDueCards(userId, level, sourceType, categories, studyDirection)
            }

            val progressSnapshot = progressCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("studyDirection", studyDirection)
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
            
            Result.success(dueCards)
        } catch (e: Exception) {
            Log.e(TAG, "Errore in getDueCards: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateCardProgress(
        userId: String, 
        vocabularyId: String, 
        currentProgress: UserProgress?, 
        days: Int,
        studyDirection: String
    ): Result<Unit> {
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
                intervalDays = days,
                studyDirection = studyDirection
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
            seedInitialData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetSingleCardProgress(progressId: String): Result<Unit> {
        return try {
            progressCollection.document(progressId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedInitialData() {
        try {
            val snapshot = vocabularyCollection.whereEqualTo("sourceType", "DEFAULT").get().await()
            val existingDocs = snapshot.documents.associateBy { it.getString("italian") ?: "" }

            for (item in initialItems) {
                val existingDoc = existingDocs[item.italian]
                if (existingDoc == null) {
                    vocabularyCollection.add(item).await()
                } else {
                    val currentCats = (existingDoc.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    if (currentCats.isEmpty() || currentCats != item.categories) {
                        existingDoc.reference.update("categories", item.categories).await()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore seeding: ${e.message}")
        }
    }
}
