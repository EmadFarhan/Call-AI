package com.emad.smartdialer.ui

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.emad.smartdialer.backup.EncryptedBackupWorker
import com.emad.smartdialer.settings.SettingsRepository
import com.emad.smartdialer.telephony.SpamRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val repo = remember { SettingsRepository(ctx) }
    val scope = rememberCoroutineScope()

    var autoAnswer by remember { mutableStateOf(false) }
    var blockHidden by remember { mutableStateOf(true) }
    var simInfo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repo.autoAnswer.collect { autoAnswer = it }
    }
    LaunchedEffect(Unit) {
        repo.blockHidden.collect { blockHidden = it }
    }
    LaunchedEffect(Unit) {
        val sm = ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        simInfo = sm.activeSubscriptionInfoList?.joinToString("\n") {
            "شريحة " + (it.displayName ?: it.subscriptionId) + " — " + it.number
        } ?: "لا شرائح"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("الذكاء الاصطناعي", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الرد الآلي الذكي عند الانشغال")
            Switch(checked = autoAnswer, onCheckedChange = { v ->
                autoAnswer = v; scope.launch { repo.setAutoAnswer(v) }
            })
        }

        Text("الحظر", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("حظر الأرقام المخفية")
            Switch(checked = blockHidden, onCheckedChange = { v ->
                blockHidden = v; scope.launch { repo.setBlockHidden(v) }
            })
        }
        OutlinedButton(onClick = {
            SpamRepository.add(ctx, com.emad.smartdialer.data.BlacklistEntry(
                value = "+964", isPrefix = true, note = "مثال: حظر بادئة"
            ))
        }) { Text("إضافة بادئة للحظر (مثال +964)") }

        Text("الشريحة والشبكة", style = MaterialTheme.typography.titleMedium)
        Text(simInfo, style = MaterialTheme.typography.bodySmall)

        Text("النسخ الاحتياطي", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            WorkManager.getInstance(ctx)
                .enqueue(OneTimeWorkRequestBuilder<EncryptedBackupWorker>().build())
        }) { Text("إنشاء نسخة احتياطية مشفرة الآن") }

        Text("الثيمات", style = MaterialTheme.typography.titleMedium)
        Text("الوضع الليلي يتبع النظام تلقائياً، وعلى Android 12+ يُستخدم لون النظام الديناميكي.")
    }
}
