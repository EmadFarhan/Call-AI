package com.emad.smartdialer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.emad.smartdialer.telephony.SpamRepository
import com.emad.smartdialer.ui.*
import com.emad.smartdialer.ui.theme.SmartDialerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SpamRepository.init(applicationContext)
        setContent {
            SmartDialerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScaffold()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(2) } // اللوحة هي البداية

    // طلب الصلاحيات الأساسية عند أول تشغيل
    val permissions = buildList {
        add(Manifest.permission.READ_CALL_LOG)
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.READ_SMS)
        add(Manifest.permission.CALL_PHONE)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}
    LaunchedEffect(Unit) { launcher.launch(permissions) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("المتصل الذكي") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("السجل") })
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("جهات الاتصال") })
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Call, null) },
                    label = { Text("لوحة الاتصال") })
                NavigationBarItem(
                    selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(Icons.Default.Email, null) },
                    label = { Text("الرسائل") })
                NavigationBarItem(
                    selected = tab == 4, onClick = { tab = 4 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("الإعدادات") })
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> RecentCallsScreen()
                1 -> ContactsScreen()
                2 -> DialerScreen()
                3 -> MessagesScreen()
                4 -> SettingsScreen()
            }
        }
    }
}
