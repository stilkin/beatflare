package be.pocito.glyphsense.model

data class VisualizerSettings(
    val brightness: Float = 1.0f,              // 0.0 .. 1.0
    val zoneCEnabled: Boolean = true,          // spectrum (20 LEDs)
    val zoneAEnabled: Boolean = true,          // bass VU (11 LEDs)
    val zoneBEnabled: Boolean = true,          // beat flash (5 LEDs)
    val partyTheme: PartyTheme = PartyTheme.SPECTRUM,
    val beaconHue: Float = 0f,                 // 0..360°; Beacon background colour is HSV(beaconHue, 1, brightness)
    val beaconText: String = "",               // empty = no centred text on Beacon overlay
    val beaconTextColor: BeaconTextColor = BeaconTextColor.WHITE,
    val beaconReactToSound: Boolean = true,    // when true, Beacon brightness is audio-driven
    val glyphsOutputEnabled: Boolean = true,   // drive glyph LEDs on start (Nothing devices only)
    val partyOutputEnabled: Boolean = true,    // launch front-screen Show overlay on start
    val flashEnabled: Boolean = false,         // drive the rear camera torch from audio (opt-in)
    val flashIntensity: Float = 0.1f,          // 0.1..1 ceiling on torch brightness (low default — torch is bright)
)
