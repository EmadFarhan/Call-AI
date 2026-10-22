package com.emad.smartdialer.ai

import kotlin.math.sqrt

interface TextEmbedder {
    val dimensions: Int
    fun embed(text: String): FloatArray
}

fun cosine(a: FloatArray, b: FloatArray): Float {
    var dot = 0f; var na = 0f; var nb = 0f
    val n = minOf(a.size, b.size)
    for (i in 0 until n) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
    if (na == 0f || nb == 0f) return 0f
    return dot / (sqrt(na) * sqrt(nb))
}

/**
 * تضمين تجزيئي بسيط (Bag-of-hashed-ngrams) — يجعل البحث الدلالي يعمل فوراً.
 * TODO(المرحلة 2): استبدله بنموذج تضمين حقيقي متعدد اللغات (مثل multilingual-e5 عبر ONNX Runtime).
 */
class HashingEmbedder(override val dimensions: Int = 384) : TextEmbedder {

    override fun embed(text: String): FloatArray {
        val v = FloatArray(dimensions)
        val grams = tokenize(text)
        for (g in grams) v[Math.floorMod(g.hashCode(), dimensions)] += 1f
        // كلمات أكثر أهمية (أسماء/أرقام) تُضاعف وزنها
        for (g in grams.filter { it.any(Char::isDigit) || it.length > 5 }) {
            v[Math.floorMod(g.hashCode(), dimensions)] += 1f
        }
        return v
    }

    private fun tokenize(t: String): List<String> {
        val clean = t.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
        return clean.split(Regex("\\s+")).filter { it.length >= 2 } +
                clean.split(Regex("\\s+")).filter { it.length >= 4 }
                    .flatMap { w -> (0..w.length - 3).map { w.substring(it, it + 3) } }
    }
}
