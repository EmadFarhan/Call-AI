package com.emad.smartdialer.ai

interface LocalLlmEngine {
    suspend fun generate(prompt: String): String
}

/**
 * محرك قاعدة-قواعد يعمل بدون أي نموذج خارجي.
 * TODO(المرحلة 2): استبدله بنموذج على الجهاز (MediaPipe LLM Inference / Gemini Nano via AICore)
 * بنفس الواجهة — لن يتغير أي كود آخر.
 */
class RuleBasedLlmEngine : LocalLlmEngine {

    override suspend fun generate(prompt: String): String {
        val transcript = prompt.substringAfterLast("المكالمة:").trim()
        val summary = summarize(transcript)
        val tasks = extractTasks(transcript)
        return "الملخص: $summary\nالمهام:\n" + tasks.joinToString("\n") { "- $it" }
    }

    private fun summarize(t: String): String {
        val first = t.lines().firstOrNull { it.isNotBlank() } ?: "مكالمة بدون تفريغ."
        return if (first.length > 120) first.take(120) + "…" else first
    }

    private fun extractTasks(t: String): List<String> {
        val markers = listOf("تأكد", "أرسل", "ارجع", "اتصل", "سلّم", "حوّل", "راجع", "اكتب")
        return t.lines().filter { line -> markers.any { line.contains(it) } }
            .map { it.trim().take(80) }
            .ifEmpty { listOf("لا مهام واضحة — راجع التفريغ الكامل") }
    }
}
