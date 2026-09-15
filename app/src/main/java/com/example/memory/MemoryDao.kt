package com.example.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM jarvis_memory ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM jarvis_memory WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM jarvis_memory WHERE content LIKE '%' || :query || '%' OR memoryKey LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Query("SELECT * FROM jarvis_memory ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentMemories(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM jarvis_memory WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM jarvis_memory")
    suspend fun clearAllMemories()

    // Smart Clipboard "clips" table queries
    @Query("SELECT * FROM clips ORDER BY timestamp DESC")
    fun getAllClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentClips(limit: Int = 3): Flow<List<ClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    @Query("SELECT * FROM clips ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastClip(): ClipEntity?

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteClip(id: Long)
}
