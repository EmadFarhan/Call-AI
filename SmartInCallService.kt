package com.emad.smartdialer.telephony

import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.emad.smartdialer.ai.AICallAssistant
import com.emad.smartdialer.ai.LiveTranscriptBus
import com.emad.smartdialer.ai.SmartSummaryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartInCallService : InCallService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var assistant: AICallAssistant? = null
    private var lastCallId: String? = null

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        val number = call.details.handle?.schemeSpecificPart ?: "unknown"
        lastCallId = number + "_" + System.currentTimeMillis()

        if (shouldAutoAnswer(number)) {
            call.answer(VideoProfile.STATE_AUDIO_ONLY)
            startAssistant()
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        scope.launch(Dispatchers.Default) {
            lastCallId?.let { SmartSummaryUseCase(this@SmartInCallService).summarize(it) }
            assistant?.stop()
            assistant = null
        }
    }

    private fun shouldAutoAnswer(number: String): Boolean {
        // TODO: اربطها بـ SettingsRepository + قواعد (مثلاً الأرقام غير المخزونة فقط)
        return false
    }

    private fun startAssistant() {
        assistant = AICallAssistant(
            ctx = this,
            onIntent = { LiveTranscriptBus.setIntent(it) }
        ).also { it.start() }
    }
}
