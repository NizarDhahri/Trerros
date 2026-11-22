package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AudioSettings
import com.example.model.GladeCatalog
import com.example.model.GladeId
import com.example.model.TimeOfDayMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.willowlightDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "willowlight_sanctuary"
)

data class PersistedGroveSnapshot(
    val activeGladeId: GladeId,
    val dewlight: Int,
    val litLanternIds: Set<String>,
    val unlockedKeepsakeIds: Set<String>,
    val activeKeepsakeIds: Set<String>,
    val claimedSpiritGiftIds: Set<String>,
    val mood: TimeOfDayMood,
    val audio: AudioSettings
)

/**
 * Offline-first repository backed by Preferences DataStore for persisting sanctuary progress
 * and independent audio settings across sessions.
 */
class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_GLADE = stringPreferencesKey("active_glade")
        val DEWLIGHT = intPreferencesKey("dewlight")
        val LIT_LANTERNS = stringSetPreferencesKey("lit_lanterns")
        val UNLOCKED_KEEPSAKES = stringSetPreferencesKey("unlocked_keepsakes")
        val ACTIVE_KEEPSAKES = stringSetPreferencesKey("active_keepsakes")
        val CLAIMED_SPIRITS = stringSetPreferencesKey("claimed_spirits")
        val MOOD = stringPreferencesKey("time_of_day_mood")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val MUSIC_MUTED = booleanPreferencesKey("music_muted")
        val SFX_MUTED = booleanPreferencesKey("sfx_muted")
    }

    val snapshotFlow: Flow<PersistedGroveSnapshot> = context.willowlightDataStore.data.map { prefs ->
        val savedLit = prefs[Keys.LIT_LANTERNS]
        val litLanterns = if (savedLit == null) {
            GladeCatalog.defaultLitLanternIds
        } else {
            savedLit + GladeCatalog.defaultLitLanternIds
        }

        PersistedGroveSnapshot(
            activeGladeId = GladeId.fromKey(prefs[Keys.ACTIVE_GLADE]),
            dewlight = prefs[Keys.DEWLIGHT] ?: 4,
            litLanternIds = litLanterns,
            unlockedKeepsakeIds = prefs[Keys.UNLOCKED_KEEPSAKES] ?: emptySet(),
            activeKeepsakeIds = prefs[Keys.ACTIVE_KEEPSAKES] ?: emptySet(),
            claimedSpiritGiftIds = prefs[Keys.CLAIMED_SPIRITS] ?: emptySet(),
            mood = TimeOfDayMood.fromName(prefs[Keys.MOOD]),
            audio = AudioSettings(
                musicVolume = (prefs[Keys.MUSIC_VOLUME] ?: 0.65f).coerceIn(0f, 1f),
                sfxVolume = (prefs[Keys.SFX_VOLUME] ?: 0.75f).coerceIn(0f, 1f),
                isMusicMuted = prefs[Keys.MUSIC_MUTED] ?: false,
                isSfxMuted = prefs[Keys.SFX_MUTED] ?: false
            )
        )
    }

    suspend fun saveProgress(
        activeGladeId: GladeId,
        dewlight: Int,
        litLanternIds: Set<String>,
        unlockedKeepsakeIds: Set<String>,
        activeKeepsakeIds: Set<String>,
        claimedSpiritGiftIds: Set<String>
    ) {
        context.willowlightDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_GLADE] = activeGladeId.storageKey
            prefs[Keys.DEWLIGHT] = dewlight.coerceAtLeast(0)
            prefs[Keys.LIT_LANTERNS] = litLanternIds
            prefs[Keys.UNLOCKED_KEEPSAKES] = unlockedKeepsakeIds
            prefs[Keys.ACTIVE_KEEPSAKES] = activeKeepsakeIds
            prefs[Keys.CLAIMED_SPIRITS] = claimedSpiritGiftIds
        }
    }

    suspend fun saveMood(mood: TimeOfDayMood) {
        context.willowlightDataStore.edit { prefs ->
            prefs[Keys.MOOD] = mood.name
        }
    }

    suspend fun saveAudioSettings(audio: AudioSettings) {
        context.willowlightDataStore.edit { prefs ->
            prefs[Keys.MUSIC_VOLUME] = audio.musicVolume.coerceIn(0f, 1f)
            prefs[Keys.SFX_VOLUME] = audio.sfxVolume.coerceIn(0f, 1f)
            prefs[Keys.MUSIC_MUTED] = audio.isMusicMuted
            prefs[Keys.SFX_MUTED] = audio.isSfxMuted
        }
    }

    suspend fun resetSanctuaryProgress() {
        context.willowlightDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_GLADE] = GladeId.POSTHOUSE.storageKey
            prefs[Keys.DEWLIGHT] = 4
            prefs[Keys.LIT_LANTERNS] = GladeCatalog.defaultLitLanternIds
            prefs[Keys.UNLOCKED_KEEPSAKES] = emptySet()
            prefs[Keys.ACTIVE_KEEPSAKES] = emptySet()
            prefs[Keys.CLAIMED_SPIRITS] = emptySet()
        }
    }
}
