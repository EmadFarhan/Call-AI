package com.emad.smartdialer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist")
data class BlacklistEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: String,          // رقم كامل أو بادئة (مثل +96477)
    val isPrefix: Boolean = false,
    val note: String = ""
)

@Entity(tableName = "call_embeddings")
data class CallEmbedding(
    @PrimaryKey val sourceId: String,   // مُعرّف المكالمة/الرسالة
    val type: String,                   // "call" | "sms"
    val vector: String                  // أرقام مفصولة بفواصل (384 بُعداً)
)

@Entity(tableName = "summary_tasks")
data class SummaryTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callId: String,
    val title: String,
    val done: Boolean = false
)

@Entity(tableName = "blocked_calls")
data class BlockedCall(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
