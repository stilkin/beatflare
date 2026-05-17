package be.pocito.glyphsense.model

data class VisualizerSettings(
    val brightness: Float = 1.0f,              // 0.0 .. 1.0
    val zoneCEnabled: Boolean = true,          // spectrum (20 LEDs)
    val zoneAEnabled: Boolean = true,          // bass VU (11 LEDs)
    val zoneBEnabled: Boolean = true,          // beat flash (5 LEDs)
    val partyTheme: PartyTheme = PartyTheme.SPECTRUM,
    val monoColor: Int = 0xFFFFFFFF.toInt(),   // ARGB; Mono theme uses this as its base color
    val partyOverlayText: String = "",         // empty = no character overlay on party screen
    val glyphsOutputEnabled: Boolean = true,   // drive glyph LEDs on start (Nothing devices only)
    val partyOutputEnabled: Boolean = true,    // launch front-screen party mode on start
)

