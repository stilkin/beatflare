package be.pocito.glyphsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BeatFlareColorScheme = darkColorScheme(
    primary = BeatFlareMagenta,
    secondary = BeatFlareOrange,
    tertiary = BeatFlarePink,
    background = BeatFlareBackground,
    surface = BeatFlareSurface,
    surfaceVariant = Color(0xFF2A2A2C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BeatFlareOnSurface,
    onSurface = BeatFlareOnSurface,
    onSurfaceVariant = BeatFlareOnSurfaceDim,
)

@Composable
fun GlyphSenseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BeatFlareColorScheme,
        typography = Typography,
        content = content,
    )
}
