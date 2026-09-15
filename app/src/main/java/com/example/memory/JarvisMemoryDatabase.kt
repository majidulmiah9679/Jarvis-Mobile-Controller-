package com.example.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room SQLite Database for long-term memory persistence.
 */
@Database(entities = [MemoryEntity::class, ClipEntity::class], version = 2, exportSchema = false)
abstract class JarvisMemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisMemoryDatabase? = null

        fun getDatabase(context: Context): JarvisMemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisMemoryDatabase::class.java,
                    "jarvis_memory_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
