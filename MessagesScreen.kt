package com.emad.smartdialer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Telephony
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.text.format.DateUtils
import com.emad.smartdialer.sms.SmsThreatAnalyzer
import com.emad.smartdialer.sms.Threat

data class SmsEntry(val address: String, val body: String, val date: Long)

@SuppressLint("Range")
fun querySms(ctx: Context): List<SmsEntry> {
    val list = mutableListOf<SmsEntry>()
    val cursor = ctx.contentResolver.query(
        Telephony.Sms.Inbox.CONTENT_URI,
        arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
        null, null, Telephony.Sms.DATE + " DESC LIMIT 100"
    ) ?: return list
    cursor.use {
        while (it.moveToNext()) {
            list.add(SmsEntry(it.getString(0) ?: "", it.getString(1) ?: "", it.getLong(2)))
        }
    }
    return list
}

@Composable
fun MessagesScreen() {
    val ctx = LocalContext.current
    val messages by produceState<List<SmsEntry>>(emptyList()) {
        value = querySms(ctx)
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(messages) { m ->
            val threat = SmsThreatAnalyzer.analyze(m.body)
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(m.address, style = MaterialTheme.typography.titleMedium)
                        Text(m.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        Text(DateUtils.getRelativeTimeSpanString(m.date).toString(),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    when (threat) {
                        is Threat.Phishing, is Threat.SuspiciousLink ->
                            Icon(Icons.Default.Warning, contentDescription = "مشبوه",
                                tint = Color(0xFFB26A00))
                        is Threat.Otp ->
                            AssistChip(onClick = {}, label = { Text("رمز: " + threat.code) })
                        Threat.Safe -> {}
                    }
                }
            }
        }
    }
}
