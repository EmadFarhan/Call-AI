package com.emad.smartdialer.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.emad.smartdialer.MainActivity

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body = messages.joinToString("") { it.messageBody }
        val from = messages.firstOrNull()?.originatingAddress ?: return

        when (val threat = SmsThreatAnalyzer.analyze(body)) {
            is Threat.Phishing, is Threat.SuspiciousLink ->
                notifyThreat(context, from, (threat as Threat).let {
                    when (it) { is Threat.Phishing -> it.reason; is Threat.SuspiciousLink -> it.reason; else -> "" }
                })
            is Threat.Otp -> { /* يمكن حفظه تلقائياً في مدير الرموز */ }
            Threat.Safe -> {}
        }
    }

    private fun notifyThreat(context: Context, from: String, reason: String) {
        val channelId = "sms_threats"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "تنبيهات الاحتيال", NotificationManager.IMPORTANCE_HIGH))
        }
        val pi = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(from.hashCode(), NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("رسالة مشبوهة من $from")
            .setContentText(reason)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build())
    }
}
