package be.pocito.glyphsense.model

import android.content.Context

/**
 * Persists [VisualizerSettings] to SharedPreferences.
 * Stateless utility — the MutableStateFlow in the service companion is the source of truth.
 */
object SettingsStore {

    private const val PREFS_NAME = "beatflare_settings"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_ZONE_C = "zone_c_enabled"
    private const val KEY_ZONE_A = "zone_a_enabled"
    private const val KEY_ZONE_B = "zone_b_enabled"
    private const val KEY_PARTY_THEME = "party_theme"
    private const val KEY_MONO_COLOR = "mono_color"
    private const val KEY_OVERLAY_TEXT = "party_overlay_text"
    private const val KEY_GLYPHS_OUT = "glyphs_output_enabled"
    private const val KEY_PARTY_OUT = "party_output_enabled"

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
            monoColor = prefs.getInt(KEY_MONO_COLOR, 0xFFE91E8C.toInt()),
            partyOverlayText = prefs.getString(KEY_OVERLAY_TEXT, "") ?: "",
            glyphsOutputEnabled = prefs.getBoolean(KEY_GLYPHS_OUT, true),
            partyOutputEnabled = prefs.getBoolean(KEY_PARTY_OUT, true),
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
            .putInt(KEY_MONO_COLOR, settings.monoColor)
            .putString(KEY_OVERLAY_TEXT, settings.partyOverlayText)
            .putBoolean(KEY_GLYPHS_OUT, settings.glyphsOutputEnabled)
            .putBoolean(KEY_PARTY_OUT, settings.partyOutputEnabled)
            .apply()
    }
}
