package be.pocito.glyphsense.model

data class VisualizerSettings(
    val brightness: Float = 1.0f,              // 0.0 .. 1.0
    val zoneCEnabled: Boolean = true,          // spectrum (20 LEDs)
    val zoneAEnabled: Boolean = true,          // bass VU (11 LEDs)
    val zoneBEnabled: Boolean = true,          // beat flash (5 LEDs)
    val partyTheme: PartyTheme = PartyTheme.SPECTRUM,
)
