package be.pocito.glyphsense.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.pocito.glyphsense.R
import be.pocito.glyphsense.audio.AudioAnalysis
import be.pocito.glyphsense.model.ThemeContext
import be.pocito.glyphsense.service.GlyphSenseService
import java.text.BreakIterator
import java.util.Locale

/**
 * Full-screen color wash visualization driven by the selected [PartyTheme].
 * Tap anywhere to dismiss. Screen stays on while active.
 */
@Composable
fun PartyOverlay(onDismiss: () -> Unit) {
    val activity = LocalContext.current as? Activity

    // Intercept system back so it dismisses the overlay instead of finishing the activity.
    BackHandler { onDismiss() }

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
    // Drives recomposition every frame so time-based effects (quiet pulse, Rainbow cycle,
    // Breathe, Sweep) keep animating even when audio is silent and AudioAnalysis values
    // are equal frame-to-frame.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        GlyphSenseService.analysisFlow.collect { analysis ->
            latestAnalysis = analysis
            beatFlash = if (analysis.beat) 4 else (beatFlash - 1).coerceAtLeast(0)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { nowMs = System.currentTimeMillis() }
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) onDismiss()
    }

    val analysis = latestAnalysis
    val color = if (analysis != null) {
        settings.partyTheme.deriveColor(ThemeContext(analysis, beatFlash, nowMs, settings))
    } else {
        Color.Black
    }

    val overlayText = settings.partyOverlayText
    val cfg = LocalConfiguration.current
    val overlaySizeSp = (minOf(cfg.screenWidthDp, cfg.screenHeightDp) * 0.55f).sp
    // Rotate multi-symbol overlays CCW so each glyph stays large and they all fit
    // along the screen's long axis ("HI<3", "JD", etc. read bottom-to-top).
    val rotate = overlayText.isNotEmpty() && graphemeCount(overlayText) > 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
    ) {
        if (overlayText.isNotEmpty()) {
            Text(
                overlayText,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = overlaySizeSp,
                fontFamily = BungeeShade,
                softWrap = false,
                maxLines = 1,
                // Lighter shadow than before — the font already carries a built-in 3D look,
                // so the shadow just helps it stay legible on white/pale backgrounds.
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.45f),
                        offset = Offset(3f, 3f),
                        blurRadius = 6f,
                    ),
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize(unbounded = true)
                    .then(if (rotate) Modifier.rotate(-90f) else Modifier),
            )
        }
        Text(
            "Tap to exit",
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
        )
    }
}

private val BungeeShade = FontFamily(Font(R.font.bungee_shade))

/** Count user-perceived characters (grapheme clusters). Flags = 1, "❤️A" = 2. */
private fun graphemeCount(s: String): Int {
    val it = BreakIterator.getCharacterInstance(Locale.ROOT)
    it.setText(s)
    var n = 0
    while (it.next() != BreakIterator.DONE) n++
    return n
}
