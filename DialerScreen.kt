package com.emad.smartdialer.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private val KEYS = listOf(
    "1" to "", "2" to "ABC", "3" to "DEF",
    "4" to "GHI", "5" to "JKL", "6" to "MNO",
    "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
    "*" to "", "0" to "+", "#" to ""
)

/** لوحة الاتصال — نفس منطق MainActivity.java الأصلي لكن بـ Compose */
@Composable
fun DialerScreen() {
    val ctx = LocalContext.current
    var number by remember { mutableStateOf("") }

    val callPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) placeCall(ctx, number) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            number.ifEmpty { "أدخل الرقم" },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            textAlign = TextAlign.Center
        )
        KEYS.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (digit, letters) ->
                    FilledTonalButton(
                        onClick = { number += digit },
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(digit, style = MaterialTheme.typography.headlineSmall)
                            if (letters.isNotEmpty())
                                Text(letters, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { number = "" }) {
                Text("مسح", style = MaterialTheme.typography.labelLarge)
            }
            FloatingActionButton(
                onClick = {
                    if (number.isEmpty()) return@FloatingActionButton
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                        placeCall(ctx, number)
                    } else callPermission.launch(Manifest.permission.CALL_PHONE)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Call, contentDescription = "اتصال") }
            IconButton(onClick = { if (number.isNotEmpty()) number = number.dropLast(1) }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف")
            }
        }
    }
}

private fun placeCall(ctx: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)))
    ctx.startActivity(intent)
}
