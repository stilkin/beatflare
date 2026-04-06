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
import be.pocito.glyphsense.audio.AudioAnalysis
import be.pocito.glyphsense.service.GlyphSenseService

/**
 * Full-screen color wash visualization driven by the selected [PartyTheme].
 * Tap anywhere to dismiss. Screen stays on while active.
 */
@Composable
fun PartyOverlay(onDismiss: () -> Unit) {
    val activity = LocalContext.current as? Activity

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val isRunning by GlyphSenseService.isRunning.collectAsState()
    val settings by GlyphSenseService.settings.collectAsState()

    var latestAnalysis by remember { mutableStateOf<AudioAnalysis?>(null) }
    var beatFlash by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        GlyphSenseService.analysisFlow.collect { analysis ->
            latestAnalysis = analysis
            beatFlash = if (analysis.beat) 4 else (beatFlash - 1).coerceAtLeast(0)
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) onDismiss()
    }

    val analysis = latestAnalysis
    val color = if (analysis != null) {
        settings.partyTheme.deriveColor(analysis, beatFlash)
    } else {
        Color.Black
    }

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
