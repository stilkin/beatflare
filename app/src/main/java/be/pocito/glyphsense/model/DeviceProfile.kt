package be.pocito.glyphsense.model

import com.nothing.ketchum.Common

/**
 * Per-device LED zone configuration. Each Nothing Phone model has a different
 * number of LEDs and zone layout. The profile tells [GlyphDriver] which LED
 * indices to use for spectrum, bass, and beat visualization.
 *
 * Zone index arrays are derived from the Glyph SDK README.
 */
data class DeviceProfile(
    val name: String,
    val ledCount: Int,
    val spectrumIndices: IntArray,
    val bassIndices: IntArray?,
    val beatIndices: IntArray?,
) {
    val spectrumBands: Int get() = spectrumIndices.size

    // Profiles are static singletons — name uniquely identifies a profile.
    // IntArray fields don't need comparison since profiles are never
    // constructed dynamically with the same name but different indices.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceProfile) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    companion object {
        /**
         * Detect the current device and return the matching profile.
         * Returns null on non-Nothing devices.
         */
        fun detect(): DeviceProfile? = when {
            Common.is24111() -> PHONE_3A
            Common.is20111() -> PHONE_1
            Common.is22111() -> PHONE_2
            Common.is23111() -> PHONE_2A
            Common.is23113() -> PHONE_2A_PLUS
            Common.is25111() -> PHONE_4A
            else -> null
        }

        // ─────────────── Profile definitions ───────────────
        // All index mappings come from the SDK README:
        // https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit

        /** Phone (1): 15 LEDs. A1=0, B1=1, C1-C4=2-5, E1=6, D1_1-D1_8=7-14 */
        val PHONE_1 = DeviceProfile(
            name = "Phone (1)",
            ledCount = 15,
            spectrumIndices = intArrayOf(7, 8, 9, 10, 11, 12, 13, 14), // D1 (8 LEDs, bottom→top)
            bassIndices = intArrayOf(2, 3, 4, 5),                       // C1-C4
            beatIndices = intArrayOf(0, 1, 6),                          // A1, B1, E1
        )

        /** Phone (2): 33 LEDs. A1=0, A2=1, B1=2, C1_1-C1_16=3-18, C2-C6=19-23, E1=24, D1_1-D1_8=25-32 */
        val PHONE_2 = DeviceProfile(
            name = "Phone (2)",
            ledCount = 33,
            spectrumIndices = (3..18).toList().toIntArray(),            // C1 (16 LEDs)
            bassIndices = (25..32).toList().toIntArray(),               // D1 (8 LEDs, bottom→top)
            beatIndices = intArrayOf(0, 1, 2, 24),                     // A1, A2, B1, E1
        )

        /** Phone (2a): 26 LEDs. C1-C24=0-23, B=24, A=25 */
        val PHONE_2A = DeviceProfile(
            name = "Phone (2a)",
            ledCount = 26,
            spectrumIndices = (0..23).toList().toIntArray(),            // C (24 LEDs)
            bassIndices = intArrayOf(25),                               // A (1 LED)
            beatIndices = intArrayOf(24),                               // B (1 LED)
        )

        /** Phone (2a) Plus: same layout as Phone (2a) */
        val PHONE_2A_PLUS = DeviceProfile(
            name = "Phone (2a) Plus",
            ledCount = 26,
            spectrumIndices = (0..23).toList().toIntArray(),
            bassIndices = intArrayOf(25),
            beatIndices = intArrayOf(24),
        )

        /** Phone (3a) / (3a) Pro: 36 LEDs. C1-C20=0-19, A1-A11=20-30, B1-B5=31-35 */
        val PHONE_3A = DeviceProfile(
            name = "Phone (3a)",
            ledCount = 36,
            spectrumIndices = (0..19).toList().toIntArray(),            // C (20 LEDs)
            bassIndices = (20..30).toList().toIntArray(),               // A (11 LEDs, top→bottom)
            beatIndices = (31..35).toList().toIntArray(),               // B (5 LEDs)
        )

        /** Phone (4a): 6 LEDs. A1-A6=0-5. Too few for separate zones — all spectrum. */
        val PHONE_4A = DeviceProfile(
            name = "Phone (4a)",
            ledCount = 6,
            spectrumIndices = (0..5).toList().toIntArray(),             // A (6 LEDs)
            bassIndices = null,
            beatIndices = null,
        )
    }
}
