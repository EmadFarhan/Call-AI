package com.emad.smartdialer.sms

sealed interface Threat {
    object Safe : Threat
    data class Phishing(val reason: String) : Threat
    data class SuspiciousLink(val reason: String) : Threat
    data class Otp(val code: String) : Threat
}

/** تحليل محتوى الرسائل لكشف الاحتيال والروابط الخبيثة */
object SmsThreatAnalyzer {

    private val phishingWords = listOf(
        "رابط", "اضغط", "تحقق", "urgent", "verify", "winner",
        "congratulation", "حسابك", "معلقة", "urgent"
    )
    private val shorteners = listOf("bit.ly/", "tinyurl.", "t.co/", "goo.gl/")
    private val otpPattern = Regex("(?i)(code|رمز|كود)[:\s]*([0-9]{4,8})")

    fun analyze(body: String): Threat {
        val lower = body.lowercase()
        otpPattern.find(body)?.let { return Threat.Otp(it.groupValues[2]) }
        if (phishingWords.any { lower.contains(it) }) return Threat.Phishing("محتوى احتيالي محتمل")
        if (shorteners.any { lower.contains(it) }) return Threat.SuspiciousLink("رابط مختصر غير موثوق")
        return Threat.Safe
    }
}
