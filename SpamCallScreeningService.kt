package com.emad.smartdialer.telephony

import android.telecom.Call
import android.telecom.CallScreeningService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * فلترة الاحتيال على مستوى النظام: القرار يُتَّخذ قبل رنين الهاتف.
 * يعمل فقط عند منح التطبيق دور "تطبيق فحص المكالمات" من إعدادات النظام.
 */
class SpamCallScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart ?: return respondAllow(details)

        when {
            SpamRepository.isBlocked(number) -> {
                scope.launch { SpamRepository.recordBlock(this@SpamCallScreeningService, number, "blacklist") }
                respondToCall(details, CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build())
            }
            SpamRepository.isSuspected(number) -> {
                scope.launch { SpamRepository.recordBlock(this@SpamCallScreeningService, number, "spam-suspected") }
                respondToCall(details, CallResponse.Builder()
                    .setSilenceCall(true)          // صامت + إشعار مشبوه
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipNotification(false)
                    .build())
            }
            else -> respondAllow(details)
        }
    }

    private fun respondAllow(details: Call.Details) =
        respondToCall(details, CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .build())
}
