package com.emad.smartdialer.telephony

import android.content.Context
import com.emad.smartdialer.data.AppDatabase
import com.emad.smartdialer.data.BlacklistEntry
import com.emad.smartdialer.data.BlockedCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * كاش في الذاكرة للأرقام المحظورة حتى لا نحظر خيط النظام.
 * يُزامَن تلقائياً مع Room عند أي تعديل.
 */
object SpamRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var exact = emptySet<String>()
    @Volatile private var prefixes = emptyList<String>()
    @Volatile private var blockHidden = true
    @Volatile private var knownSpam = setOf("+964771000000") // قاعدة بذرية — تُحدَّث لاحقاً

    fun init(ctx: Context) = scope.launch {
        val db = AppDatabase.get(ctx)
        reload(db)
        blockHidden = true
    }

    private suspend fun reload(db: AppDatabase) {
        val list = db.blacklistDao().all()
        exact = list.filter { !it.isPrefix }.map { normalize(it.value) }.toSet()
        prefixes = list.filter { it.isPrefix }.map { normalize(it.value) }
    }

    fun isBlocked(number: String): Boolean {
        val n = normalize(number)
        if (n.isEmpty()) return blockHidden
        return exact.contains(n) || prefixes.any { n.startsWith(it) }
    }

    fun isSuspected(number: String): Boolean = normalize(number) in knownSpam

    fun add(ctx: Context, entry: BlacklistEntry) = scope.launch {
        val db = AppDatabase.get(ctx)
        db.blacklistDao().upsert(entry)
        reload(db)
    }

    fun remove(ctx: Context, id: Long) = scope.launch {
        val db = AppDatabase.get(ctx)
        db.blacklistDao().delete(id)
        reload(db)
    }

    suspend fun recordBlock(ctx: Context, number: String, reason: String) {
        AppDatabase.get(ctx).blockedCallDao()
            .insert(BlockedCall(number = number, reason = reason))
    }

    private fun normalize(n: String) =
        n.replace(" ", "").replace("-", "").removePrefix("tel:")
}
