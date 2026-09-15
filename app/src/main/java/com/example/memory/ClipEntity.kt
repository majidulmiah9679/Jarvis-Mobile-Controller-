package com.example.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for SMART CLIPBOARD feature: stores copied clips in "clips" table.
 */
@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
