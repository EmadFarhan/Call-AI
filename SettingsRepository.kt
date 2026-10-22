package com.emad.smartdialer.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val ctx: Context) {

    private val KEY_AUTO_ANSWER = booleanPreferencesKey("auto_answer")
    private val KEY_BLOCK_HIDDEN = booleanPreferencesKey("block_hidden")
    private val KEY_FORCE_DARK = booleanPreferencesKey("force_dark")
    private val KEY_SIM_MAP = stringPreferencesKey("sim_map") // "contactId:subId;..."

    val autoAnswer: Flow<Boolean> = ctx.dataStore.data.map { it[KEY_AUTO_ANSWER] ?: false }
    val blockHidden: Flow<Boolean> = ctx.dataStore.data.map { it[KEY_BLOCK_HIDDEN] ?: true }
    val forceDark: Flow<Boolean> = ctx.dataStore.data.map { it[KEY_FORCE_DARK] ?: false }
    val simMap: Flow<Map<Long, Int>> = ctx.dataStore.data.map { p ->
        (p[KEY_SIM_MAP] ?: "").split(';').filter { it.contains(':') }
            .associate {
                val (k, v) = it.split(':')
                k.toLong() to v.toInt()
            }
    }

    suspend fun setAutoAnswer(v: Boolean) = ctx.dataStore.edit { it[KEY_AUTO_ANSWER] = v }
    suspend fun setBlockHidden(v: Boolean) = ctx.dataStore.edit { it[KEY_BLOCK_HIDDEN] = v }
    suspend fun setForceDark(v: Boolean) = ctx.dataStore.edit { it[KEY_FORCE_DARK] = v }

    suspend fun setSimForContact(contactId: Long, subId: Int) {
        ctx.dataStore.edit { p ->
            val map = (p[KEY_SIM_MAP] ?: "").split(';').filter { it.contains(':') }
                .associate { val (k, v) = it.split(':'); k.toLong() to v.toInt() }
                .toMutableMap()
            map[contactId] = subId
            p[KEY_SIM_MAP] = map.entries.joinToString(";") { (k, v) -> "$k:$v" }
        }
    }
}
