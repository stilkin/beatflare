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
    private const val KEY_BEACON_HUE = "beacon_hue"
    private const val KEY_BEACON_TEXT = "beacon_text"
    private const val KEY_BEACON_TEXT_COLOR = "beacon_text_color"
    private const val KEY_BEACON_REACT = "beacon_react_to_sound"
    private const val KEY_GLYPHS_OUT = "glyphs_output_enabled"
    private const val KEY_PARTY_OUT = "party_output_enabled"

    // Legacy keys from before the Beacon rework. Read once for migration, then removed.
    private const val LEGACY_MONO_COLOR = "mono_color"
    private const val LEGACY_OVERLAY_TEXT = "party_overlay_text"

    fun load(context: Context): VisualizerSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Detect a stored MONOCHROME theme and remap it to SPECTRUM.
        val rawTheme = prefs.getString(KEY_PARTY_THEME, null)
        val themeWasMono = rawTheme == "MONOCHROME"
        val partyTheme = if (themeWasMono) {
            PartyTheme.SPECTRUM
        } else {
            rawTheme?.let { name -> PartyTheme.entries.find { it.name == name } } ?: PartyTheme.SPECTRUM
        }

        // beaconHue: prefer new key; otherwise migrate from legacy monoColor (hue only).
        val hasNewHue = prefs.contains(KEY_BEACON_HUE)
        val hasLegacyColor = prefs.contains(LEGACY_MONO_COLOR)
        val beaconHue = when {
            hasNewHue -> prefs.getFloat(KEY_BEACON_HUE, 0f)
            hasLegacyColor -> argbToHue(prefs.getInt(LEGACY_MONO_COLOR, 0xFFE91E8C.toInt()))
            else -> 0f
        }

        // beaconText: prefer new key; otherwise migrate from legacy overlay text.
        val hasNewText = prefs.contains(KEY_BEACON_TEXT)
        val hasLegacyText = prefs.contains(LEGACY_OVERLAY_TEXT)
        val beaconText = when {
            hasNewText -> prefs.getString(KEY_BEACON_TEXT, "") ?: ""
            hasLegacyText -> prefs.getString(LEGACY_OVERLAY_TEXT, "") ?: ""
            else -> ""
        }

        val beaconTextColor = prefs.getString(KEY_BEACON_TEXT_COLOR, null)
            ?.let { name -> BeaconTextColor.entries.find { it.name == name } }
            ?: BeaconTextColor.WHITE

        val settings = VisualizerSettings(
            brightness = prefs.getFloat(KEY_BRIGHTNESS, 1.0f),
            zoneCEnabled = prefs.getBoolean(KEY_ZONE_C, true),
            zoneAEnabled = prefs.getBoolean(KEY_ZONE_A, true),
            zoneBEnabled = prefs.getBoolean(KEY_ZONE_B, true),
            partyTheme = partyTheme,
            beaconHue = beaconHue,
            beaconText = beaconText,
            beaconTextColor = beaconTextColor,
            beaconReactToSound = prefs.getBoolean(KEY_BEACON_REACT, true),
            glyphsOutputEnabled = prefs.getBoolean(KEY_GLYPHS_OUT, true),
            partyOutputEnabled = prefs.getBoolean(KEY_PARTY_OUT, true),
        )

        // One-shot migration cleanup: write the new shape and clear legacy keys.
        if (themeWasMono || hasLegacyColor || hasLegacyText) {
            save(context, settings)
            prefs.edit()
                .remove(LEGACY_MONO_COLOR)
                .remove(LEGACY_OVERLAY_TEXT)
                .apply()
        }

        return settings
    }

    fun save(context: Context, settings: VisualizerSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BRIGHTNESS, settings.brightness)
            .putBoolean(KEY_ZONE_C, settings.zoneCEnabled)
            .putBoolean(KEY_ZONE_A, settings.zoneAEnabled)
            .putBoolean(KEY_ZONE_B, settings.zoneBEnabled)
            .putString(KEY_PARTY_THEME, settings.partyTheme.name)
            .putFloat(KEY_BEACON_HUE, settings.beaconHue)
            .putString(KEY_BEACON_TEXT, settings.beaconText)
            .putString(KEY_BEACON_TEXT_COLOR, settings.beaconTextColor.name)
            .putBoolean(KEY_BEACON_REACT, settings.beaconReactToSound)
            .putBoolean(KEY_GLYPHS_OUT, settings.glyphsOutputEnabled)
            .putBoolean(KEY_PARTY_OUT, settings.partyOutputEnabled)
            .apply()
    }

    /** Extract the hue (0..360°) from a packed ARGB int. Returns 0f for greyscale colours. */
    private fun argbToHue(argb: Int): Float {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        if (delta == 0f) return 0f
        val h = when (max) {
            r -> 60f * (((g - b) / delta) % 6f)
            g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        return if (h < 0f) h + 360f else h
    }
}
