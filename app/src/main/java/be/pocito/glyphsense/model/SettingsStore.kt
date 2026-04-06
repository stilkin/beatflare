package be.pocito.glyphsense.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists [VisualizerSettings] to [SharedPreferences].
 * Stateless utility — the [MutableStateFlow] in the service companion is the source of truth.
 */
object SettingsStore {

    private const val PREFS_NAME = "beatflare_settings"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_ZONE_C = "zone_c_enabled"
    private const val KEY_ZONE_A = "zone_a_enabled"
    private const val KEY_ZONE_B = "zone_b_enabled"
    private const val KEY_PARTY_THEME = "party_theme"

    fun load(context: Context): VisualizerSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return VisualizerSettings(
            brightness = prefs.getFloat(KEY_BRIGHTNESS, 1.0f),
            zoneCEnabled = prefs.getBoolean(KEY_ZONE_C, true),
            zoneAEnabled = prefs.getBoolean(KEY_ZONE_A, true),
            zoneBEnabled = prefs.getBoolean(KEY_ZONE_B, true),
            partyTheme = prefs.getString(KEY_PARTY_THEME, null)
                ?.let { name -> PartyTheme.entries.find { it.name == name } }
                ?: PartyTheme.SPECTRUM,
        )
    }

    fun save(context: Context, settings: VisualizerSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BRIGHTNESS, settings.brightness)
            .putBoolean(KEY_ZONE_C, settings.zoneCEnabled)
            .putBoolean(KEY_ZONE_A, settings.zoneAEnabled)
            .putBoolean(KEY_ZONE_B, settings.zoneBEnabled)
            .putString(KEY_PARTY_THEME, settings.partyTheme.name)
            .apply()
    }
}
