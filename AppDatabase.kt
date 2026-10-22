package com.emad.smartdialer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BlacklistEntry::class, CallEmbedding::class, SummaryTask::class, BlockedCall::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blacklistDao(): BlacklistDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun taskDao(): TaskDao
    abstract fun blockedCallDao(): BlockedCallDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(ctx, AppDatabase::class.java, "smartdialer.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
