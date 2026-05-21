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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import be.pocito.glyphsense.ui.theme.BeatFlareMagenta
import be.pocito.glyphsense.ui.theme.BeatFlareOnSurfaceDim
import kotlin.math.roundToInt

/**
 * Hue picker for the Beacon background. Saturation is locked to 1.0 so the colour
 * is always vivid; brightness comes from the React-to-sound modulation at render time.
 */
@Composable
fun BeaconHuePicker(hue: Float, onHueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.hsv(hue, 1f, 1f)),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Hue",
                style = MaterialTheme.typography.bodySmall,
                color = BeatFlareOnSurfaceDim,
                modifier = Modifier.width(44.dp).padding(start = 4.dp),
            )
            Slider(
                value = hue,
                onValueChange = onHueChange,
                valueRange = 0f..360f,
                steps = 71,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = BeatFlareMagenta,
                    activeTrackColor = BeatFlareMagenta,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Text(
                "${hue.roundToInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = BeatFlareOnSurfaceDim,
                modifier = Modifier.width(44.dp),
            )
        }
    }
}
