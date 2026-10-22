package com.emad.smartdialer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
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

data class CallEntry(val name: String?, val number: String, val type: Int, val date: Long)

@SuppressLint("Range")
fun queryCallLog(ctx: Context): List<CallEntry> {
    val list = mutableListOf<CallEntry>()
    val cursor = ctx.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE, CallLog.Calls.DATE),
        null, null, CallLog.Calls.DATE + " DESC LIMIT 50"
    ) ?: return list
    cursor.use {
        while (it.moveToNext()) {
            list.add(CallEntry(
                name = it.getString(0),
                number = it.getString(1) ?: "",
                type = it.getInt(2),
                date = it.getLong(3)
            ))
        }
    }
    return list
}

@Composable
fun RecentCallsScreen() {
    val ctx = LocalContext.current
    val calls by produceState<List<CallEntry>>(emptyList()) {
        value = queryCallLog(ctx)
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { AiSummaryCard() }
        items(calls) { call ->
            val missed = call.type == CallLog.Calls.MISSED_TYPE ||
                         call.type == CallLog.Calls.REJECTED_TYPE
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(call.name ?: call.number,
                            color = if (missed) MaterialTheme.colorScheme.error
                                    else MaterialTheme.typography.titleMedium.color)
                        Text(call.number, style = MaterialTheme.typography.bodySmall)
                    }
                    if (call.type == CallLog.Calls.BLOCKED_TYPE) {
                        AssistChip(
                            onClick = {},
                            label = { Text("محظور") },
                            leadingIcon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFB26A00)) }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(DateUtils.getRelativeTimeSpanString(call.date).toString(),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/** بطاقة الملخص الذكي التجريبية */
@Composable
private fun AiSummaryCard() {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("ملخص المكالمة الذكي", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("اتفقتم على تسليم الطلب الخميس 10:00 صباحاً — تأكد من العنوان قبل الموعد.")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("مهمة: تأكيد العنوان") })
                AssistChip(onClick = {}, label = { Text("مهمة: إرسال الموقع") })
            }
        }
    }
}
