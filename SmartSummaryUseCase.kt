package com.emad.smartdialer.ai

import android.content.Context
import com.emad.smartdialer.data.AppDatabase
import com.emad.smartdialer.data.SummaryTask

/** ملخص المكالمة الذكي + استخراج المهام وتخزينها */
class SmartSummaryUseCase(ctx: Context) {

    private val db = AppDatabase.get(ctx)
    private val llm: LocalLlmEngine = RuleBasedLlmEngine()

    suspend fun summarize(callId: String): Pair<String, List<SummaryTask>> {
        val transcript = LiveTranscriptBus.lines.value.joinToString("\n") { it.text }
        val prompt = "لخّص هذه المكالمة واستخرج المهام:\nالمكالمة:\n$transcript"
        val output = llm.generate(prompt)

        val summary = output.substringAfter("الملخص:").substringBefore("المهام:").trim()
        val tasks = output.substringAfter("المهام:")
            .lines().filter { it.trimStart().startsWith("-") }
            .map { it.trim().removePrefix("-").trim() }
            .map { SummaryTask(callId = callId, title = it) }

        db.taskDao().insertAll(tasks)
        return summary to tasks
    }
}
