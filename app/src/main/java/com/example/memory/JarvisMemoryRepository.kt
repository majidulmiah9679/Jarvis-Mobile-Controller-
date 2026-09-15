package com.example.memory

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * Long-Term Vector & SQLite Memory Repository for J.A.R.V.I.S.
 * Handles persistence of facts, preferences, contacts, and semantic retrieval.
 */
class JarvisMemoryRepository(context: Context) {

    private val database = JarvisMemoryDatabase.getDatabase(context)
    private val dao = database.memoryDao()

    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()

    companion object {
        const val CATEGORY_USER_FACT = "USER_FACT"
        const val CATEGORY_PREFERENCE = "PREFERENCE"
        const val CATEGORY_CONTACT = "CONTACT"
        const val CATEGORY_COMMAND = "COMMAND"
        const val CATEGORY_NOTE = "NOTE"
        private const val VECTOR_DIM = 64
    }

    suspend fun saveFact(key: String, content: String): Long = withContext(Dispatchers.IO) {
        val vector = generateLocalEmbedding(content)
        val entity = MemoryEntity(
            category = CATEGORY_USER_FACT,
            memoryKey = key,
            content = content,
            embeddingVector = vectorToString(vector)
        )
        dao.insertMemory(entity)
    }

    suspend fun savePreference(key: String, content: String): Long = withContext(Dispatchers.IO) {
        val vector = generateLocalEmbedding(content)
        val entity = MemoryEntity(
            category = CATEGORY_PREFERENCE,
            memoryKey = key,
            content = content,
            embeddingVector = vectorToString(vector)
        )
        dao.insertMemory(entity)
    }

    suspend fun saveContact(name: String, details: String): Long = withContext(Dispatchers.IO) {
        val vector = generateLocalEmbedding("$name $details")
        val entity = MemoryEntity(
            category = CATEGORY_CONTACT,
            memoryKey = name,
            content = details,
            embeddingVector = vectorToString(vector)
        )
        dao.insertMemory(entity)
    }

    suspend fun saveCommand(command: String, response: String): Long = withContext(Dispatchers.IO) {
        val vector = generateLocalEmbedding(command)
        val entity = MemoryEntity(
            category = CATEGORY_COMMAND,
            memoryKey = command.take(30),
            content = "User: $command -> Response: ${response.take(150)}",
            embeddingVector = vectorToString(vector)
        )
        dao.insertMemory(entity)
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMemory(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAllMemories()
    }

    /**
     * Semantic & Keyword Hybrid Retrieval.
     * Computes cosine similarity of query against local memory vectors and extracts
     * top relevant facts to inject into the LLM context window.
     */
    suspend fun retrieveRelevantContext(query: String, topK: Int = 4): String = withContext(Dispatchers.IO) {
        try {
            val queryVector = generateLocalEmbedding(query)
            val recentMemories = dao.getRecentMemories()
            if (recentMemories.isEmpty()) return@withContext ""

            val scoredList = recentMemories.map { memory ->
                val memVector = stringToVector(memory.embeddingVector)
                val similarity = if (memVector.isNotEmpty()) {
                    cosineSimilarity(queryVector, memVector)
                } else 0f

                // Add keyword bonus
                val keywordBonus = if (query.split(" ").any { word ->
                    word.length > 3 && memory.content.contains(word, ignoreCase = true)
                }) 0.25f else 0.0f

                val finalScore = similarity + keywordBonus
                Pair(memory, finalScore)
            }

            // Filter elements with reasonable relevance and take topK
            val topMemories = scoredList
                .sortedByDescending { it.second }
                .take(topK)
                .filter { it.second > 0.15f }
                .map { "[${it.first.category}] ${it.first.memoryKey}: ${it.first.content}" }

            if (topMemories.isEmpty()) "" else topMemories.joinToString("\n")
        } catch (e: Exception) {
            ""
        }
    }

    // -------------------------------------------------------------
    // Local Lightweight Fast Vector Embedding & Math
    // -------------------------------------------------------------
    private fun generateLocalEmbedding(text: String): FloatArray {
        val vector = FloatArray(VECTOR_DIM)
        val clean = text.lowercase().trim()
        if (clean.isEmpty()) return vector

        // Trigram and character hash distribution
        val tokens = clean.split("\\s+".toRegex())
        for (token in tokens) {
            val hash = token.hashCode()
            val index = (hash % VECTOR_DIM + VECTOR_DIM) % VECTOR_DIM
            vector[index] += 1.0f

            // Sub-character n-grams for typo & morphological resilience
            for (i in 0 until (token.length - 2).coerceAtLeast(0)) {
                val tri = token.substring(i, i + 3)
                val triHash = (tri.hashCode() % VECTOR_DIM + VECTOR_DIM) % VECTOR_DIM
                vector[triHash] += 0.5f
            }
        }

        // L2 Normalize vector
        var norm = 0.0
        for (v in vector) {
            norm += v * v
        }
        val length = sqrt(norm).toFloat()
        if (length > 0.0001f) {
            for (i in vector.indices) {
                vector[i] /= length
            }
        }
        return vector
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size || v1.isEmpty()) return 0f
        var dot = 0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
        }
        return dot.coerceIn(-1.0f, 1.0f)
    }

    private fun vectorToString(vector: FloatArray): String {
        return vector.joinToString(",") { "%.3f".format(it) }
    }

    private fun stringToVector(str: String): FloatArray {
        if (str.isBlank()) return FloatArray(0)
        return try {
            str.split(",").map { it.toFloatOrNull() ?: 0f }.toFloatArray()
        } catch (e: Exception) {
            FloatArray(0)
        }
    }

    // Smart Clipboard repository functions
    val recentClips: Flow<List<ClipEntity>> = dao.getRecentClips(3)
    val allClips: Flow<List<ClipEntity>> = dao.getAllClips()

    suspend fun saveClip(text: String): Long = withContext(Dispatchers.IO) {
        dao.insertClip(ClipEntity(text = text))
    }

    suspend fun getLastClip(): ClipEntity? = withContext(Dispatchers.IO) {
        dao.getLastClip()
    }

    suspend fun deleteClip(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteClip(id)
    }
}
