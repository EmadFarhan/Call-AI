package com.emad.smartdialer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM blacklist")
    suspend fun all(): List<BlacklistEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BlacklistEntry)

    @Query("DELETE FROM blacklist WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface EmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: CallEmbedding)

    @Query("SELECT * FROM call_embeddings")
    suspend fun all(): List<CallEmbedding>

    @Query("SELECT * FROM call_embeddings WHERE sourceId = :id")
    suspend fun bySource(id: String): CallEmbedding?
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<SummaryTask>)

    @Query("SELECT * FROM summary_tasks WHERE callId = :callId")
    suspend fun forCall(callId: String): List<SummaryTask>
}

@Dao
interface BlockedCallDao {
    @Insert
    suspend fun insert(b: BlockedCall)

    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC LIMIT 50")
    suspend fun recent(): List<BlockedCall>
}
