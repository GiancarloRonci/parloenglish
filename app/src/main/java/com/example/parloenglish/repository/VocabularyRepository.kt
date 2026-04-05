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
        // --- LEVEL A1 ---
        VocabularyItem(italian = "Ciao", english = "Hello", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Grazie", english = "Thank you", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Per favore", english = "Please", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Sì", english = "Yes", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "No", english = "No", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Mi chiamo...", english = "My name is...", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Introductions")),
        VocabularyItem(italian = "Come stai?", english = "How are you?", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),
        VocabularyItem(italian = "Piacere di conoscerti", english = "Nice to meet you", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Introductions")),
        VocabularyItem(italian = "Scusa", english = "Excuse me", level = "A1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Arrivederci", english = "Goodbye", level = "A1", sourceType = "DEFAULT", categories = listOf("General", "Greetings")),

        // --- LEVEL A2 ---
        VocabularyItem(italian = "Colazione", english = "Breakfast", level = "A2", sourceType = "DEFAULT", categories = listOf("Food", "Daily Life")),
        VocabularyItem(italian = "Pranzo", english = "Lunch", level = "A2", sourceType = "DEFAULT", categories = listOf("Food")),
        VocabularyItem(italian = "Cena", english = "Dinner", level = "A2", sourceType = "DEFAULT", categories = listOf("Food")),
        VocabularyItem(italian = "Dove si trova?", english = "Where is it located?", level = "A2", sourceType = "DEFAULT", categories = listOf("Travel", "Directions")),
        VocabularyItem(italian = "Posso avere il conto?", english = "Can I have the bill?", level = "A2", sourceType = "DEFAULT", categories = listOf("Travel", "Food")),
        VocabularyItem(italian = "Quanto costa?", english = "How much does it cost?", level = "A2", sourceType = "DEFAULT", categories = listOf("Shopping")),
        VocabularyItem(italian = "Mi piace viaggiare", english = "I like traveling", level = "A2", sourceType = "DEFAULT", categories = listOf("General", "Hobbies")),
        VocabularyItem(italian = "Ho fame", english = "I am hungry", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life")),
        VocabularyItem(italian = "Fa freddo oggi", english = "It is cold today", level = "A2", sourceType = "DEFAULT", categories = listOf("Weather")),
        VocabularyItem(italian = "Voglio andare a casa", english = "I want to go home", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life")),
        VocabularyItem(italian = "Aspettare l'autobus", english = "To wait for the bus", level = "A2", sourceType = "DEFAULT", categories = listOf("Travel")),
        VocabularyItem(italian = "Guardare la TV", english = "To watch TV", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life", "Hobbies")),
        VocabularyItem(italian = "Ascoltare musica", english = "To listen to music", level = "A2", sourceType = "DEFAULT", categories = listOf("Hobbies")),
        VocabularyItem(italian = "Cucinare", english = "To cook", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life", "Hobbies")),
        VocabularyItem(italian = "Comprare vestiti", english = "To buy clothes", level = "A2", sourceType = "DEFAULT", categories = listOf("Shopping")),
        VocabularyItem(italian = "Stanco", english = "Tired", level = "A2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Felice", english = "Happy", level = "A2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Triste", english = "Sad", level = "A2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Arrabbiato", english = "Angry", level = "A2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Prendere il treno", english = "To take the train", level = "A2", sourceType = "DEFAULT", categories = listOf("Travel")),

        // --- LEVEL B1 ---
        VocabularyItem(italian = "In effetti", english = "Actually", level = "B1", sourceType = "DEFAULT", categories = listOf("Connectors", "Social")),
        VocabularyItem(italian = "Per fortuna", english = "Luckily", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Decisione difficile", english = "Difficult decision", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Esperienza lavorativa", english = "Work experience", level = "B1", sourceType = "DEFAULT", categories = listOf("Work", "Business")),
        VocabularyItem(italian = "Prenotare una stanza", english = "To book a room", level = "B1", sourceType = "DEFAULT", categories = listOf("Travel")),
        VocabularyItem(italian = "Chiedere un favore", english = "To ask for a favour", level = "B1", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Essere in ritardo", english = "To be late", level = "B1", sourceType = "DEFAULT", categories = listOf("Daily Life")),
        VocabularyItem(italian = "Prendere in prestito", english = "To borrow", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Prestare", english = "To lend", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Cancellare un appuntamento", english = "To cancel an appointment", level = "B1", sourceType = "DEFAULT", categories = listOf("Work", "Social")),
        VocabularyItem(italian = "Fare del proprio meglio", english = "To do one's best", level = "B1", sourceType = "DEFAULT", categories = listOf("Idioms")),
        VocabularyItem(italian = "Prendersi cura di", english = "To take care of", level = "B1", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs")),
        VocabularyItem(italian = "Cercare (qualcosa di perso)", english = "To look for", level = "B1", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs")),
        VocabularyItem(italian = "Scoprire", english = "To find out", level = "B1", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs")),
        VocabularyItem(italian = "Andare d'accordo", english = "To get along", level = "B1", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs", "Social")),
        VocabularyItem(italian = "Rimandare a domani", english = "To postpone until tomorrow", level = "B1", sourceType = "DEFAULT", categories = listOf("Daily Life", "Work")),
        VocabularyItem(italian = "Sembrare", english = "To seem", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Evitare (qualcuno)", english = "To avoid someone", level = "B1", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Sperare", english = "To hope", level = "B1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Divertente", english = "Funny", level = "B1", sourceType = "DEFAULT", categories = listOf("General", "Social")),

        // --- LEVEL B2 ---
        VocabularyItem(italian = "Nonostante ciò", english = "Nevertheless", level = "B2", sourceType = "DEFAULT", categories = listOf("Academic", "Connectors")),
        VocabularyItem(italian = "Inoltre", english = "Furthermore", level = "B2", sourceType = "DEFAULT", categories = listOf("Academic", "Connectors")),
        VocabularyItem(italian = "Raggiungere un obiettivo", english = "To achieve a goal", level = "B2", sourceType = "DEFAULT", categories = listOf("Business", "General")),
        VocabularyItem(italian = "Rimandare", english = "To put off", level = "B2", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs")),
        VocabularyItem(italian = "Cercare di capire", english = "To figure out", level = "B2", sourceType = "DEFAULT", categories = listOf("Phrasal Verbs")),
        VocabularyItem(italian = "Valere la pena", english = "To be worth it", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms")),
        VocabularyItem(italian = "Sviluppare", english = "To develop", level = "B2", sourceType = "DEFAULT", categories = listOf("Business")),
        VocabularyItem(italian = "Approccio", english = "Approach", level = "B2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Scopo", english = "Purpose", level = "B2", sourceType = "DEFAULT", categories = listOf("General", "Business")),
        VocabularyItem(italian = "Migliorare", english = "To improve", level = "B2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Sfida", english = "Challenge", level = "B2", sourceType = "DEFAULT", categories = listOf("General", "Business")),
        VocabularyItem(italian = "Ambiente", english = "Environment", level = "B2", sourceType = "DEFAULT", categories = listOf("Nature")),
        VocabularyItem(italian = "Comportamento", english = "Behaviour", level = "B2", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Aumentare", english = "To increase", level = "B2", sourceType = "DEFAULT", categories = listOf("Business")),
        VocabularyItem(italian = "Opportunità", english = "Opportunity", level = "B2", sourceType = "DEFAULT", categories = listOf("Business")),
        VocabularyItem(italian = "Suggerire", english = "To suggest", level = "B2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Rifiutare", english = "To refuse", level = "B2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Consigliare", english = "To advise", level = "B2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Lamentarsi", english = "To complain", level = "B2", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Preoccupato", english = "Concerned", level = "B2", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Gestire", english = "To manage", level = "B2", sourceType = "DEFAULT", categories = listOf("Business", "Work")),
        VocabularyItem(italian = "Convincere", english = "To convince", level = "B2", sourceType = "DEFAULT", categories = listOf("Social", "Business")),
        VocabularyItem(italian = "Testardo", english = "Stubborn", level = "B2", sourceType = "DEFAULT", categories = listOf("General", "Social")),
        VocabularyItem(italian = "Affidabile", english = "Reliable", level = "B2", sourceType = "DEFAULT", categories = listOf("Social", "Business")),
        VocabularyItem(italian = "Volubile (umore)", english = "Moody", level = "B2", sourceType = "DEFAULT", categories = listOf("General", "Social")),
        VocabularyItem(italian = "Evidente (chiaro)", english = "Apparent", level = "B2", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Ignorare", english = "To ignore", level = "B2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Apprezzare", english = "To appreciate", level = "B2", sourceType = "DEFAULT", categories = listOf("Social", "General")),
        VocabularyItem(italian = "Incoraggiare (qualcuno)", english = "To encourage someone", level = "B2", sourceType = "DEFAULT", categories = listOf("Social")),
        VocabularyItem(italian = "Evitare di fare qualcosa", english = "To avoid doing something", level = "B2", sourceType = "DEFAULT", categories = listOf("Grammar", "General")),

        // --- LEVEL C1 ---
        VocabularyItem(italian = "Fondamentale", english = "Essential", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "General")),
        VocabularyItem(italian = "Comprensibile", english = "Comprehensive", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Sorprendente", english = "Astonishing", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Incoraggiare (promuovere)", english = "To foster", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "Business")),
        VocabularyItem(italian = "Attuare", english = "To implement", level = "C1", sourceType = "DEFAULT", categories = listOf("Business", "Academic")),
        VocabularyItem(italian = "Notevole", english = "Remarkable", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Valutare", english = "To assess", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "Business")),
        VocabularyItem(italian = "Involontariamente", english = "Unintentionally", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Accentuare", english = "To emphasize", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Sostanziale", english = "Substantial", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Business")),
        VocabularyItem(italian = "Pertinente", english = "Relevant", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "Business")),
        VocabularyItem(italian = "Incentivo", english = "Incentive", level = "C1", sourceType = "DEFAULT", categories = listOf("Business")),
        VocabularyItem(italian = "Iniziativa", english = "Initiative", level = "C1", sourceType = "DEFAULT", categories = listOf("Business")),
        VocabularyItem(italian = "Trascurare", english = "To overlook", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Affrontare (una sfida)", english = "To tackle", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Business")),
        VocabularyItem(italian = "Vulnerabile", english = "Vulnerable", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Social")),
        VocabularyItem(italian = "Spontaneo", english = "Spontaneous", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Ipotesi", english = "Hypothesis", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Risultato finale", english = "Outcome", level = "C1", sourceType = "DEFAULT", categories = listOf("Business", "Academic")),
        VocabularyItem(italian = "Coerente", english = "Consistent", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Academic")),

        // --- LEVEL C2 ---
        VocabularyItem(italian = "Ambiguo", english = "Ambiguous", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Inherentemente", english = "Inherently", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Incessante", english = "Relentless", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Metaforicamente", english = "Metaphorically", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Oscurare", english = "To obscure", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Paradosso", english = "Paradox", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Pragmatico", english = "Pragmatic", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic", "Business")),
        VocabularyItem(italian = "Sottile (differenza)", english = "Subtle", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Onnipresente", english = "Ubiquitous", level = "C2", sourceType = "DEFAULT", categories = listOf("General", "Technology")),
        VocabularyItem(italian = "Profondo", english = "Profound", level = "C2", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Indubbiamente", english = "Undoubtedly", level = "C2", sourceType = "DEFAULT", categories = listOf("Connectors", "Academic")),
        VocabularyItem(italian = "Inevitabile", english = "Inevitable", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Inconcepibile", english = "Inconceivable", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Eloquente", english = "Eloquent", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Prestigioso", english = "Prestigious", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Enigmatico", english = "Enigmatic", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Meticoloso", english = "Meticulous", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Resilienza", english = "Resilience", level = "C2", sourceType = "DEFAULT", categories = listOf("Social", "General")),
        VocabularyItem(italian = "Preminente", english = "Prominent", level = "C2", sourceType = "DEFAULT", categories = listOf("General", "Business")),
        VocabularyItem(italian = "Superfluo", english = "Superfluous", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),

        // --- ADDITIONAL CARDS TO REACH 200+ ---
        VocabularyItem(italian = "Aeroporto", english = "Airport", level = "A2", sourceType = "DEFAULT", categories = listOf("Travel")),
        VocabularyItem(italian = "Supermercato", english = "Supermarket", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life", "Shopping")),
        VocabularyItem(italian = "Ospedale", english = "Hospital", level = "A2", sourceType = "DEFAULT", categories = listOf("Daily Life")),
        VocabularyItem(italian = "Ufficio", english = "Office", level = "A2", sourceType = "DEFAULT", categories = listOf("Work")),
        VocabularyItem(italian = "Biblioteca", english = "Library", level = "A2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Ambizione", english = "Ambition", level = "B1", sourceType = "DEFAULT", categories = listOf("Business", "General")),
        VocabularyItem(italian = "Inquinamento", english = "Pollution", level = "B1", sourceType = "DEFAULT", categories = listOf("Nature")),
        VocabularyItem(italian = "Ricerca", english = "Research", level = "B1", sourceType = "DEFAULT", categories = listOf("Academic")),
        VocabularyItem(italian = "Evidenza", english = "Evidence", level = "B1", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Prevedere", english = "To predict", level = "B1", sourceType = "DEFAULT", categories = listOf("General", "Technology")),
        VocabularyItem(italian = "Menagramo", english = "Jinx", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms", "Slang")),
        VocabularyItem(italian = "Pezzo di torta (facilissimo)", english = "A piece of cake", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms")),
        VocabularyItem(italian = "Girare intorno al punto", english = "To beat around the bush", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms")),
        VocabularyItem(italian = "In un batter d'occhio", english = "In the blink of an eye", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms")),
        VocabularyItem(italian = "Rompere il ghiaccio", english = "To break the ice", level = "B2", sourceType = "DEFAULT", categories = listOf("Idioms", "Social")),
        VocabularyItem(italian = "Cyber-sicurezza", english = "Cybersecurity", level = "B2", sourceType = "DEFAULT", categories = listOf("Technology", "Business")),
        VocabularyItem(italian = "Intelligenza Artificiale", english = "Artificial Intelligence", level = "B2", sourceType = "DEFAULT", categories = listOf("Technology")),
        VocabularyItem(italian = "Innovare", english = "To innovate", level = "B2", sourceType = "DEFAULT", categories = listOf("Business", "Technology")),
        VocabularyItem(italian = "Trasmettere", english = "To broadcast", level = "B2", sourceType = "DEFAULT", categories = listOf("Technology", "General")),
        VocabularyItem(italian = "Aggiornare", english = "To update", level = "B2", sourceType = "DEFAULT", categories = listOf("Technology", "Daily Life")),
        VocabularyItem(italian = "Perspicace", english = "Insightful", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "General")),
        VocabularyItem(italian = "Contraddittorio", english = "Contradictory", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "General")),
        VocabularyItem(italian = "Sperimentale", english = "Experimental", level = "C1", sourceType = "DEFAULT", categories = listOf("Academic", "Science")),
        VocabularyItem(italian = "Infrastruttura", english = "Infrastructure", level = "C1", sourceType = "DEFAULT", categories = listOf("Business", "General")),
        VocabularyItem(italian = "Convincente", english = "Compelling", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Social")),
        VocabularyItem(italian = "Autonomia (personale)", english = "Self-governance", level = "C1", sourceType = "DEFAULT", categories = listOf("Social", "Politics")),
        VocabularyItem(italian = "Prevedibile", english = "Predictable", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Incoerente", english = "Inconsistent", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Paradossale (situazione)", english = "Ironical", level = "C1", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Autentico", english = "Authentic", level = "C1", sourceType = "DEFAULT", categories = listOf("General", "Social")),
        VocabularyItem(italian = "Efemero", english = "Ephemeral", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic", "General")),
        VocabularyItem(italian = "Inappellabile", english = "Unappealable", level = "C2", sourceType = "DEFAULT", categories = listOf("Law", "Academic")),
        VocabularyItem(italian = "Sincronizzare (dati)", english = "To sync", level = "C2", sourceType = "DEFAULT", categories = listOf("Technology")),
        VocabularyItem(italian = "Incomparabile (unico)", english = "Matchless", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Infallibile", english = "Infallible", level = "C2", sourceType = "DEFAULT", categories = listOf("General")),
        VocabularyItem(italian = "Spregevole", english = "Despicable", level = "C2", sourceType = "DEFAULT", categories = listOf("Social", "General")),
        VocabularyItem(italian = "Metodicamente", english = "Methodically", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic", "Work")),
        VocabularyItem(italian = "Perspicacia", english = "Perspicacity", level = "C2", sourceType = "DEFAULT", categories = listOf("Academic", "General")),
        VocabularyItem(italian = "Imprescindibile", english = "Indispensable", level = "C2", sourceType = "DEFAULT", categories = listOf("General", "Academic")),
        VocabularyItem(italian = "Anacronistico", english = "Anachronistic", level = "C2", sourceType = "DEFAULT", categories = listOf("History", "Academic"))
    )

    /**
     * Retrieves all unique categories from the database.
     */
    suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val snapshot = vocabularyCollection.get().await()
            val categories = snapshot.documents
                .flatMap { (it.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList() }
                .distinct()
                .filter { it.isNotEmpty() }
                .sorted()
            
            Log.d(TAG, "Categories found in DB: $categories")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error getAllCategories: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches vocabulary items filtered by levels, source type, and categories.
     */
    suspend fun getVocabularyFiltered(levels: List<String>, sourceType: String?, categories: List<String>?): Result<List<VocabularyItem>> {
        return try {
            var query = vocabularyCollection.whereIn("level", levels)
            if (sourceType != null && sourceType != "ALL") {
                query = query.whereEqualTo("sourceType", sourceType)
            }
            
            val snapshot = query.get().await()
            var items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VocabularyItem::class.java)?.copy(id = doc.id)
            }
            
            // Manual filter for categories (more flexible than Firestore array filter)
            if (!categories.isNullOrEmpty()) {
                items = items.filter { item ->
                    item.categories.any { it in categories }
                }
            }
            
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error getVocabularyFiltered: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Retrieves all vocabulary items along with user progress for a specific study direction.
     */
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

    /**
     * Retrieves cards that are due for review based on SRS logic and filters.
     */
    suspend fun getDueCards(
        userId: String, 
        levels: List<String>, 
        sourceType: String? = null,
        categories: List<String>? = null,
        studyDirection: String = "IT_TO_EN"
    ): Result<List<Pair<VocabularyItem, UserProgress?>>> {
        return try {
            val allVocab = getVocabularyFiltered(levels, sourceType, categories).getOrThrow()
            
            // If nothing found and looking for system cards, ensure database is seeded
            if (allVocab.isEmpty() && (sourceType == "DEFAULT" || sourceType == "ALL" || sourceType == null)) {
                seedInitialData()
                val retryVocab = getVocabularyFiltered(levels, sourceType, categories).getOrThrow()
                if (retryVocab.isEmpty()) return Result.success(emptyList())
                return getDueCards(userId, levels, sourceType, categories, studyDirection)
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
            Log.e(TAG, "Error in getDueCards: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates card progress in Firestore based on user's selected interval.
     */
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

    /**
     * Resets all progress for a specific user.
     */
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

    /**
     * Resets progress for a single card.
     */
    suspend fun resetSingleCardProgress(progressId: String): Result<Unit> {
        return try {
            progressCollection.document(progressId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Seeds the initial vocabulary data into Firestore if missing or outdated.
     */
    suspend fun seedInitialData() {
        try {
            Log.d(TAG, "Starting system data verification and update...")
            val snapshot = vocabularyCollection.whereEqualTo("sourceType", "DEFAULT").get().await()
            val existingDocs = snapshot.documents.associateBy { it.getString("italian") ?: "" }

            for (item in initialItems) {
                val existingDoc = existingDocs[item.italian]
                if (existingDoc == null) {
                    Log.d(TAG, "Inserting new word: ${item.italian}")
                    vocabularyCollection.add(item).await()
                } else {
                    val currentCats = (existingDoc.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val currentLevel = existingDoc.getString("level") ?: ""
                    
                    if (currentCats.sorted() != item.categories.sorted() || currentLevel != item.level) {
                        Log.d(TAG, "Updating fields for: ${item.italian}")
                        val updates = mapOf(
                            "categories" to item.categories,
                            "level" to item.level
                        )
                        existingDoc.reference.update(updates).await()
                    }
                }
            }
            Log.d(TAG, "Verification completed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during seed: ${e.message}")
        }
    }
}
