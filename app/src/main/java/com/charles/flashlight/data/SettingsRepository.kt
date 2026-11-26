package com.charles.flashlight.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "flashlight_settings")

class SettingsRepository(private val context: Context) {

    val shakeToToggle: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SHAKE] ?: false
    }

    val hapticsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_HAPTICS] ?: true
    }

    val soundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SOUND] ?: false
    }

    /** Auto-off delay in minutes; 0 = disabled. */
    val autoOffMinutes: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_AUTO_OFF_MIN] ?: 0
    }

    /**
     * Half-period in ms for strobe on/off (symmetrical). Default 70 matches legacy timing.
     * Range 30..150 ms.
     */
    val strobeHalfPeriodMs: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        (prefs[KEY_STROBE_HALF_MS] ?: DEFAULT_STROBE_HALF_MS).coerceIn(STROBE_HALF_MIN, STROBE_HALF_MAX)
    }

    val launchCount: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_LAUNCH_COUNT] ?: 0
    }

    /** True once the user has acknowledged the photosensitive-epilepsy warning for flashing modes. */
    val strobeWarningAcknowledged: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_STROBE_WARNING_ACK] ?: false
    }

    /** Number of completed torch sessions (torch turned on, then off again). Drives review timing. */
    val successfulSessionCount: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SESSION_COUNT] ?: 0
    }

    suspend fun setShakeToToggle(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_SHAKE] = value }
    }

    suspend fun setHapticsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_HAPTICS] = value }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_SOUND] = value }
    }

    suspend fun setAutoOffMinutes(minutes: Int) {
        context.settingsDataStore.edit { it[KEY_AUTO_OFF_MIN] = minutes.coerceIn(0, 60) }
    }

    suspend fun setStrobeHalfPeriodMs(ms: Int) {
        context.settingsDataStore.edit {
            it[KEY_STROBE_HALF_MS] = ms.coerceIn(STROBE_HALF_MIN, STROBE_HALF_MAX)
        }
    }

    suspend fun setStrobeWarningAcknowledged(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_STROBE_WARNING_ACK] = value }
    }

    suspend fun incrementSuccessfulSessionCount(): Int {
        val holder = intArrayOf(0)
        context.settingsDataStore.edit { prefs ->
            val cur = prefs[KEY_SESSION_COUNT] ?: 0
            holder[0] = cur + 1
            prefs[KEY_SESSION_COUNT] = holder[0]
        }
        return holder[0]
    }

    suspend fun incrementLaunchCount(): Int {
        val holder = intArrayOf(0)
        context.settingsDataStore.edit { prefs ->
            val cur = prefs[KEY_LAUNCH_COUNT] ?: 0
            holder[0] = cur + 1
            prefs[KEY_LAUNCH_COUNT] = holder[0]
        }
        return holder[0]
    }

    companion object {
        private val KEY_SHAKE = booleanPreferencesKey("shake_to_toggle")
        private val KEY_HAPTICS = booleanPreferencesKey("haptics")
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_AUTO_OFF_MIN = intPreferencesKey("auto_off_minutes")
        private val KEY_STROBE_HALF_MS = intPreferencesKey("strobe_half_period_ms")
        private val KEY_LAUNCH_COUNT = intPreferencesKey("launch_count")
        private val KEY_STROBE_WARNING_ACK = booleanPreferencesKey("strobe_warning_acknowledged")
        private val KEY_SESSION_COUNT = intPreferencesKey("successful_session_count")

        const val STROBE_HALF_MIN = 30
        const val STROBE_HALF_MAX = 150
        const val DEFAULT_STROBE_HALF_MS = 70

        /** Named strobe presets (half-period ms). Never faster than [STROBE_HALF_MIN]. */
        val STROBE_PRESETS_MS: List<Int> = listOf(130, 90, 50)
    }
}
