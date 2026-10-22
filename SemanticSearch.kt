package com.emad.smartdialer.search

import android.content.Context
import com.emad.smartdialer.ai.HashingEmbedder
import com.emad.smartdialer.ai.TextEmbedder
import com.emad.smartdialer.ai.cosine
import com.emad.smartdialer.data.AppDatabase
import com.emad.smartdialer.data.CallEmbedding

data class SearchHit(val sourceId: String, val type: String, val score: Float)

/**
 * البحث الدلالي: "المكالمة اللي حجينا بيها عن أسعار المواد"
 * — يعمل الآن عبر التضمين التجزيئي، ويُستبدل باحقاً بنموذج حقيقي.
 */
class SemanticSearch(ctx: Context) {

    private val db = AppDatabase.get(ctx)
    private val embedder: TextEmbedder = HashingEmbedder()

    suspend fun indexContent(sourceId: String, type: String, text: String) {
        db.embeddingDao().upsert(CallEmbedding(
            sourceId = sourceId, type = type,
            vector = embedder.embed(text).joinToString(",")
        ))
    }

    suspend fun search(query: String, minScore: Float = 0.35f): List<SearchHit> {
        val q = embedder.embed(query)
        return db.embeddingDao().all().mapNotNull { e ->
            val v = e.vector.split(',').map { it.toFloat() }.toFloatArray()
            val score = cosine(q, v)
            if (score >= minScore) SearchHit(e.sourceId, e.type, score) else null
        }.sortedByDescending { it.score }
    }
}
