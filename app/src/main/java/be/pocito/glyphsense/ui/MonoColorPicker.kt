package be.pocito.glyphsense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import be.pocito.glyphsense.ui.theme.BeatFlareMagenta
import be.pocito.glyphsense.ui.theme.BeatFlareOnSurfaceDim
import kotlin.math.roundToInt

/**
 * Hue + Saturation picker for the Mono party theme. Brightness is intentionally
 * audio-driven, so the user only picks the color identity, not its intensity.
 */
@Composable
fun MonoColorPicker(
    color: Int,
    onColorChange: (Int) -> Unit,
) {
    val hsv = remember(color) { rgbToHsv(color) }
    val hue = hsv[0]
    val saturation = hsv[1]
    val preview = Color.hsv(hue, saturation, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Preview swatch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(preview),
        )

        LabeledSlider(
            label = "Hue",
            value = hue,
            valueRange = 0f..360f,
            display = "${hue.roundToInt()}°",
        ) { newHue ->
            onColorChange(Color.hsv(newHue, saturation, 1f).toArgb())
        }

        LabeledSlider(
            label = "Sat",
            value = saturation.coerceIn(0.05f, 1f),
            valueRange = 0.05f..1f,
            steps = 18,
            display = "${(saturation * 100).roundToInt()}%",
        ) { newSat ->
            onColorChange(Color.hsv(hue, newSat, 1f).toArgb())
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    steps: Int = 0,
    onChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = BeatFlareOnSurfaceDim,
            modifier = Modifier.width(44.dp).padding(start = 4.dp),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = BeatFlareMagenta,
                activeTrackColor = BeatFlareMagenta,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        Text(
            display,
            style = MaterialTheme.typography.bodySmall,
            color = BeatFlareOnSurfaceDim,
            modifier = Modifier.width(44.dp),
        )
    }
}

/**
 * Convert a packed ARGB int to [hue 0..360, saturation 0..1, value 0..1].
 * Independent of Compose Color.hsv so we can read back any stored color.
 */
private fun rgbToHsv(argb: Int): FloatArray {
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    val s = if (max == 0f) 0f else delta / max
    return floatArrayOf(h, s, max)
}
