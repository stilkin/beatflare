package be.pocito.glyphsense.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import be.pocito.glyphsense.service.GlyphSenseService

/**
 * Full-screen color wash visualization.
 *
 * - **Hue** shifts with the dominant frequency band (0–19 → 0°–360°)
 * - **Lightness** pulses with bass amplitude
 * - **Beat** triggers a brief brightness flash
 * - Tap anywhere to dismiss
 * - Screen stays on while active
 */
@Composable
fun PartyOverlay(onDismiss: () -> Unit) {
    val activity = LocalContext.current as? Activity

    // Keep screen on while party mode is active.
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Observe service state
    val isRunning by GlyphSenseService.isRunning.collectAsState()
    var bassLevel by remember { mutableStateOf(0f) }
    var spectrum by remember { mutableStateOf(FloatArray(20)) }
    var beatFlash by remember { mutableIntStateOf(0) }

    // Collect live analysis from the service
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        GlyphSenseService.analysisFlow.collect { analysis ->
            bassLevel = analysis.bassLevel
            spectrum = analysis.spectrum
            beatFlash = if (analysis.beat) 4 else (beatFlash - 1).coerceAtLeast(0)
        }
    }

    // Auto-exit if service stops
    LaunchedEffect(isRunning) {
        if (!isRunning) onDismiss()
    }

    // Derive color from analysis
    val dominantIndex = spectrum.indices.maxByOrNull { spectrum[it] } ?: 0
    val hue = dominantIndex.toFloat() / maxOf(spectrum.size, 1) * 360f
    val baseLightness = 0.10f + bassLevel * 0.55f
    val flashBoost = if (beatFlash > 0) 0.30f else 0f
    val lightness = (baseLightness + flashBoost).coerceIn(0f, 1f)

    val color = Color.hsl(
        hue = hue,
        saturation = 0.85f,
        lightness = lightness,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            "Tap to exit",
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.padding(bottom = 48.dp),
        )
    }
}
