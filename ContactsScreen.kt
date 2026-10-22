package com.emad.smartdialer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class ContactEntry(val name: String, val phone: String)

@SuppressLint("Range")
fun queryContacts(ctx: Context): List<ContactEntry> {
    val list = mutableListOf<ContactEntry>()
    val cursor = ctx.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC LIMIT 200"
    ) ?: return list
    cursor.use {
        while (it.moveToNext()) {
            list.add(ContactEntry(it.getString(0) ?: "", it.getString(1) ?: ""))
        }
    }
    return list.distinct()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen() {
    val ctx = LocalContext.current
    val contacts by produceState<List<ContactEntry>>(emptyList()) {
        value = queryContacts(ctx)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(contacts) { c ->
                Card(Modifier.fillMaxWidth().clickable {
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(c.phone))))
                }) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(c.name.take(1), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium)
                            Text(c.phone, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.Call, contentDescription = "اتصال")
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                ctx.startActivity(Intent(Intent.ACTION_INSERT)
                    .setType(ContactsContract.Contacts.CONTENT_TYPE))
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "إضافة") }
    }
}
