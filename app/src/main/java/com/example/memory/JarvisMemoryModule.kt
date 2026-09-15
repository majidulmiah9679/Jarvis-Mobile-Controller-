package com.example.memory

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * JARVIS Long-Term Memory Module.
 * Powers Room SQLite database + local vector embeddings to save user facts,
 * music preferences, contact associations, and historical command logs.
 * Provides automatic retrieval injection during every query.
 */
class JarvisMemoryModule(context: Context) {

    private val repository = JarvisMemoryRepository(context)

    val allMemories: Flow<List<MemoryEntity>> = repository.allMemories

    suspend fun saveFact(key: String, content: String): Long =
        repository.saveFact(key, content)

    suspend fun savePreference(key: String, content: String): Long =
        repository.savePreference(key, content)

    suspend fun saveContact(name: String, details: String): Long =
        repository.saveContact(name, details)

    suspend fun saveCommand(command: String, response: String): Long =
        repository.saveCommand(command, response)

    suspend fun retrieveRelevantContext(query: String, topK: Int = 3): String =
        repository.retrieveRelevantContext(query, topK)

    suspend fun wipeAllMemories() =
        repository.clearAll()
}
