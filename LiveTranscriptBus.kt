package com.emad.smartdialer.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TranscriptLine(val fromAssistant: Boolean, val text: String)

/** ناقل حي: يقرأه Compose على الشاشة أثناء المكالمة */
object LiveTranscriptBus {
    private val _lines = MutableStateFlow<List<TranscriptLine>>(emptyList())
    val lines: StateFlow<List<TranscriptLine>> = _lines

    private val _callerIntent = MutableStateFlow("")
    val callerIntent: StateFlow<String> = _callerIntent

    fun push(line: TranscriptLine) {
        _lines.value = _lines.value + line
    }

    fun setIntent(intent: String) { _callerIntent.value = intent }

    fun clear() { _lines.value = emptyList(); _callerIntent.value = "" }
}
