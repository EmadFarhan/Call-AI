package com.emad.smartdialer.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * الرد الآلي بالذكاء الاصطناعي:
 * يستمع لتفريغ كلام المتصل لايف، يستنتج سبب الاتصال،
 * ويقرأ ردودك السريعة صوتياً للمتصل.
 */
class AICallAssistant(
    private val ctx: Context,
    private val onIntent: (String) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    fun start() {
        LiveTranscriptBus.clear()
        greet()
        recognizer = SpeechRecognizer.createSpeechRecognizer(ctx).apply {
            setRecognitionListener(listener)
            startListening(intent())
        }
    }

    fun speak(text: String) {
        tts = tts ?: TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("ar")
        }
        LiveTranscriptBus.push(TranscriptLine(fromAssistant = true, text = text))
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "reply")
    }

    fun stop() {
        recognizer?.stopListening(); recognizer?.destroy(); recognizer = null
        tts?.stop(); tts?.shutdown(); tts = null
    }

    private fun greet() {
        speak("مرحباً، صاحب الهاتف مشغول حالياً. ما سبب اتصالك؟")
    }

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000)
    }

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle) {
            val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                LiveTranscriptBus.push(TranscriptLine(fromAssistant = false, text = text))
                onIntent(inferIntent(text))
            }
            recognizer?.startListening(intent()) // استمرار الاستماع
        }
        override fun onPartialResults(partial: Bundle) {
            val text = partial.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                LiveTranscriptBus.push(TranscriptLine(fromAssistant = false, text = text))
            }
        }
        override fun onError(error: Int) {
            if (error != SpeechRecognizer.ERROR_NO_MATCH) onError("STT error: $error")
            recognizer?.startListening(intent())
        }
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun inferIntent(text: String): String = when {
        listOf("طلب", "توصيل", "منتج", "بضاعة").any { text.contains(it) } -> "طلب/توصيل"
        listOf("موعد", "حجز", "عيادة", "دكتور").any { text.contains(it) } -> "حجز موعد"
        listOf("فلوس", "حوالة", "راتب", "دفعة").any { text.contains(it) } -> "مالية"
        else -> "استفسار عام"
    }
}
