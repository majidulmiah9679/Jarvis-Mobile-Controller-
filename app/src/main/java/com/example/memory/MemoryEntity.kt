package com.example.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Long-Term Persistent Memory Entity for J.A.R.V.I.S.
 * Stores user facts, preferences, contacts, commands, and local semantic vectors.
 */
@Entity(tableName = "jarvis_memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "USER_FACT", "PREFERENCE", "CONTACT", "COMMAND", "NOTE"
    val memoryKey: String,
    val content: String,
    val embeddingVector: String = "", // Comma-separated normalized float vector for local semantic matching
    val timestamp: Long = System.currentTimeMillis()
)
