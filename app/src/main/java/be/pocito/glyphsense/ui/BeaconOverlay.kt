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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.pocito.glyphsense.R
import be.pocito.glyphsense.audio.AudioAnalysis
import be.pocito.glyphsense.model.PartyTheme
import be.pocito.glyphsense.service.GlyphSenseService
import java.text.BreakIterator
import java.util.Locale

/**
 * Full-screen "find me" beacon. Renders a single hue at audio-modulated (or full) brightness,
 * with an optional centred text/emoji. Tap anywhere or press back to dismiss.
 */
@Composable
fun BeaconOverlay(onDismiss: () -> Unit) {
    val activity = LocalContext.current as? Activity

    BackHandler { onDismiss() }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val settings by GlyphSenseService.settings.collectAsState()
    val isRunning by GlyphSenseService.isRunning.collectAsState()

    var latestAnalysis by remember { mutableStateOf<AudioAnalysis?>(null) }
    // Drives recomposition every frame so the quiet pulse keeps animating during silence.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(settings.beaconReactToSound, isRunning) {
        if (!settings.beaconReactToSound || !isRunning) return@LaunchedEffect
        GlyphSenseService.analysisFlow.collect { latestAnalysis = it }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { nowMs = System.currentTimeMillis() }
        }
    }

    val brightness = if (settings.beaconReactToSound) {
        // Same shape as the old MONOCHROME intensity: floor + bass + beat + quiet pulse.
        val a = latestAnalysis
        if (a == null) {
            1f
        } else {
            (0.20f + a.bassLevel * 0.70f + PartyTheme.quietPulse(nowMs, a.bassLevel) * 0.10f)
                .coerceIn(0.10f, 1f)
        }
    } else {
        1f
    }

    val bg = Color.hsv(settings.beaconHue, 1f, brightness)

    val text = settings.beaconText
    val cfg = LocalConfiguration.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    // Rotate multi-symbol overlays CCW so each glyph stays large and they all fit
    // along the screen's long axis ("HI<3", "JD", etc. read bottom-to-top).
    val rotate = text.isNotEmpty() && graphemeCount(text) > 1
    val textSizeSp = remember(text, rotate, cfg.screenWidthDp, cfg.screenHeightDp) {
        if (text.isEmpty()) {
            0.sp
        } else {
            val refSize = 100.sp
            val layout = measurer.measure(
                text,
                TextStyle(fontFamily = BungeeShade, fontSize = refSize),
            )
            val mWdp = with(density) { layout.size.width.toDp().value }
            val mHdp = with(density) { layout.size.height.toDp().value }
            val targetW = (if (rotate) cfg.screenHeightDp else cfg.screenWidthDp) * 0.85f
            val targetH = (if (rotate) cfg.screenWidthDp else cfg.screenHeightDp) * 0.85f
            val scale = minOf(targetW / mWdp, targetH / mHdp)
            (refSize.value * scale).sp
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
    ) {
        if (text.isNotEmpty()) {
            Text(
                text,
                color = settings.beaconTextColor.color,
                fontSize = textSizeSp,
                fontFamily = BungeeShade,
                softWrap = false,
                maxLines = 1,
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
