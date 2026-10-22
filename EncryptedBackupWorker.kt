package com.emad.smartdialer.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emad.smartdialer.data.AppDatabase
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import org.json.JSONObject

/**
 * نسخ احتياطي مشفر: المفتاح في Android Keystore (لا يغادر الجهاز أبداً)،
 * والملف الناتج مشفر بـ AES256-GCM عبر Tink.
 * TODO(المرحلة 2): رفع ciphertext إلى خادمك (E2E — الخادم لا يستطيع فك التشفير).
 */
class EncryptedBackupWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = try {
        val db = AppDatabase.get(applicationContext)

        val snapshot = JSONObject().apply {
            put("version", 1)
            put("timestamp", System.currentTimeMillis())
            put("blacklist", org.json.JSONArray().apply {
                db.blacklistDao().all().forEach { put(it.value) }
            })
            put("tasks", org.json.JSONArray().apply {
                // TODO: جميع المهام عبر استعلام عام
                db.taskDao().forCall("").forEach { put(it.title) }
            })
        }
        val plaintext = snapshot.toString().encodeToByteArray()

        val ciphertext = aead().encrypt(plaintext, null)
        applicationContext.openFileOutput("backup_smartdialer.enc", Context.MODE_PRIVATE)
            .use { it.write(ciphertext) }
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }

    private fun aead(): Aead {
        AeadConfig.register()
        val kh: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(applicationContext, "backup_keyset", "smartdialer_prefs")
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://backup_master_key")
            .build().keysetHandle
        return kh.getPrimitive(Aead::class.java)
    }
}
