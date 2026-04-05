package be.pocito.glyphsense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import be.pocito.glyphsense.audio.AudioAnalyzer
import be.pocito.glyphsense.audio.AudioCapture
import be.pocito.glyphsense.audio.peakAmplitude
import be.pocito.glyphsense.glyph.GlyphController
import be.pocito.glyphsense.glyph.GlyphController.Companion.LED_COUNT_PHONE_3A
import be.pocito.glyphsense.glyph.GlyphDriver
import be.pocito.glyphsense.ui.theme.GlyphSenseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlyphSenseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { GlyphController(context) }
    val capture = remember { AudioCapture() }
    val analyzer = remember { AudioAnalyzer() }
    val driver = remember { GlyphDriver() }

    var sessionOpen by remember { mutableStateOf(false) }
    var micRunning by remember { mutableStateOf(false) }
    var drivingGlyphs by remember { mutableStateOf(false) }
    var debugExpanded by remember { mutableStateOf(false) }

    var micPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Audio analysis state (updated each frame while mic is running)
    var micPeakPct by remember { mutableIntStateOf(0) }
    var bassLevel by remember { mutableStateOf(0f) }
    var bassRaw by remember { mutableStateOf(0f) }
    var bassFloor by remember { mutableStateOf(0f) }
    var bassPeak by remember { mutableStateOf(0f) }
    var spectrum by remember { mutableStateOf(FloatArray(20)) }
    var beatFlash by remember { mutableIntStateOf(0) }

    val logLines = remember { mutableStateOf(listOf<String>()) }
    fun log(line: String) {
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        logLines.value = (listOf("[$ts] $line") + logLines.value).take(200)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
        log("RECORD_AUDIO " + if (granted) "granted" else "DENIED")
    }

    DisposableEffect(Unit) {
        log("Device: ${controller.deviceName()}")
        onDispose {
            capture.stop()
            controller.release()
        }
    }

    LaunchedEffect(micRunning) {
        if (!micRunning) return@LaunchedEffect
        try {
            capture.buffers.collect { buf ->
                val p = buf.peakAmplitude()
                micPeakPct = (p * 100 / 32767).coerceIn(0, 100)
                val analysis = analyzer.process(buf)
                bassLevel = analysis.bassLevel
                bassRaw = analysis.bassRaw
                bassFloor = analysis.bassFloor
                bassPeak = analysis.bassPeak
                spectrum = analysis.spectrum
                beatFlash = if (analysis.beat) 3 else (beatFlash - 1).coerceAtLeast(0)
                if (drivingGlyphs && sessionOpen) {
                    controller.setFrameColors(driver.render(analysis))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainScreen", "collect error: ${e.message}", e)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ───── Primary flow ─────
        Text("GlyphSense", style = MaterialTheme.typography.headlineSmall)
        StatusRow(sessionOpen, micRunning, drivingGlyphs)

        HorizontalDivider()

        // Session
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !sessionOpen,
            onClick = {
                log("Opening glyph session...")
                controller.init(
                    onReady = { sessionOpen = true; log("Session OPEN") },
                    onError = { err -> log("Session ERROR: $err") },
                )
            },
        ) { Text("Open glyph session") }

        // Mic permission (only shown if not yet granted)
        if (!micPermissionGranted) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            ) { Text("Grant mic permission") }
        }

        // Mic start/stop
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = micPermissionGranted,
            onClick = {
                if (!micRunning) {
                    capture.start()
                    micRunning = capture.isRunning()
                    log(if (micRunning) "Mic started" else "Mic FAILED to start")
                } else {
                    capture.stop()
                    micRunning = false
                    micPeakPct = 0
                    log("Mic stopped")
                }
            },
        ) { Text(if (micRunning) "Stop mic" else "Start mic") }

        // Drive glyphs
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen && micRunning,
            onClick = {
                if (!drivingGlyphs) {
                    drivingGlyphs = true
                    log("Glyph driving: ON")
                } else {
                    drivingGlyphs = false
                    controller.setFrameColors(driver.blankFrame())
                    log("Glyph driving: OFF")
                }
            },
        ) { Text(if (drivingGlyphs) "Stop driving glyphs" else "Drive glyphs from audio") }

        // ───── Live analysis (visible when mic running) ─────
        if (micRunning) {
            HorizontalDivider()
            Text("Analysis", style = MaterialTheme.typography.titleMedium)
            AnalysisDisplay(
                micPeakPct = micPeakPct,
                bassLevel = bassLevel,
                bassRaw = bassRaw,
                bassFloor = bassFloor,
                bassPeak = bassPeak,
                beatFlash = beatFlash,
                spectrum = spectrum,
            )
        }

        // ───── Debug (collapsed by default) ─────
        HorizontalDivider()
        TextButton(onClick = { debugExpanded = !debugExpanded }) {
            Text(if (debugExpanded) "▾ Debug / LED tests" else "▸ Debug / LED tests")
        }
        if (debugExpanded) {
            DebugPanel(
                sessionOpen = sessionOpen,
                onReleaseSession = {
                    controller.release()
                    sessionOpen = false
                    drivingGlyphs = false
                    log("Session released")
                },
                controller = controller,
                onLog = { log(it) },
                scope = scope,
            )
        }

        // ───── Log ─────
        HorizontalDivider()
        Text("Log", style = MaterialTheme.typography.titleMedium)
        logLines.value.forEach { line ->
            Text(
                line,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun StatusRow(sessionOpen: Boolean, micRunning: Boolean, drivingGlyphs: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "Glyph session: ${if (sessionOpen) "open" else "closed"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Microphone: ${if (micRunning) "running" else "stopped"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Driving glyphs: ${if (drivingGlyphs) "yes" else "no"}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AnalysisDisplay(
    micPeakPct: Int,
    bassLevel: Float,
    bassRaw: Float,
    bassFloor: Float,
    bassPeak: Float,
    beatFlash: Int,
    spectrum: FloatArray,
) {
    Text("Mic peak: $micPeakPct%", style = MaterialTheme.typography.bodySmall)
    LinearProgressIndicator(
        progress = { micPeakPct / 100f },
        modifier = Modifier.fillMaxWidth(),
    )

    Text("Bass: ${"%.2f".format(bassLevel)}", style = MaterialTheme.typography.bodyMedium)
    LinearProgressIndicator(
        progress = { bassLevel },
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        "  log raw=${"%.1f".format(bassRaw)}  floor=${"%.1f".format(bassFloor)}  peak=${"%.1f".format(bassPeak)}",
        style = MaterialTheme.typography.bodySmall,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Beat: ", style = MaterialTheme.typography.bodyMedium)
        val beatColor = if (beatFlash > 0) Color.Red else Color.Gray
        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
                .background(beatColor),
        )
    }

    Text("Spectrum", style = MaterialTheme.typography.bodyMedium)
    SpectrumBars(
        values = spectrum,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
    )
}

@Composable
private fun DebugPanel(
    sessionOpen: Boolean,
    onReleaseSession: () -> Unit,
    controller: GlyphController,
    onLog: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = onReleaseSession,
        ) { Text("Release glyph session") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = { controller.turnOff(); onLog("Glyphs off") },
        ) { Text("Turn off all LEDs") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A) { 4095 }
                controller.setFrameColors(arr)
                onLog("All LEDs full")
            },
        ) { Text("All LEDs full brightness") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A) { i ->
                    (i * 4095 / (LED_COUNT_PHONE_3A - 1))
                }
                controller.setFrameColors(arr)
                onLog("Gradient 0..4095")
            },
        ) { Text("Gradient 0..4095") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                scope.launch {
                    onLog("Walking idx 0..35 (400ms each)")
                    val arr = IntArray(LED_COUNT_PHONE_3A)
                    for (idx in 0 until LED_COUNT_PHONE_3A) {
                        for (i in arr.indices) arr[i] = 0
                        arr[idx] = 4095
                        controller.setFrameColors(arr)
                        kotlinx.coroutines.delay(400)
                    }
                    controller.turnOff()
                    onLog("Walk complete")
                }
            },
        ) { Text("Walk LEDs (slow chase)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                scope.launch {
                    onLog("Benchmark running (3s)...")
                    val result = withContext(Dispatchers.Default) {
                        benchmarkRefreshRate(controller, durationMs = 3000L)
                    }
                    onLog("${result.frames} frames / ${result.elapsedMs}ms = ${"%.1f".format(result.fps)} fps")
                    controller.turnOff()
                }
            },
        ) { Text("Benchmark refresh rate (3s)") }
    }
}

@Composable
private fun SpectrumBars(values: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val barWidth = w / values.size
        val gap = barWidth * 0.15f
        for (i in values.indices) {
            val v = values[i].coerceIn(0f, 1f)
            val barH = h * v
            drawRect(
                color = Color(0xFF4FC3F7),
                topLeft = Offset(i * barWidth + gap / 2f, h - barH),
                size = Size(barWidth - gap, barH),
            )
        }
    }
}

private data class BenchmarkResult(
    val frames: Int,
    val elapsedMs: Long,
    val fps: Double,
)

private fun benchmarkRefreshRate(
    controller: GlyphController,
    durationMs: Long,
): BenchmarkResult {
    val arr = IntArray(LED_COUNT_PHONE_3A)
    val start = System.nanoTime()
    val deadline = start + durationMs * 1_000_000L
    var frames = 0
    var now = start
    while (now < deadline) {
        val idx = frames % LED_COUNT_PHONE_3A
        for (i in arr.indices) arr[i] = 0
        arr[idx] = 4095
        controller.setFrameColors(arr)
        frames++
        now = System.nanoTime()
    }
    val elapsedMs = (now - start) / 1_000_000L
    val fps = if (elapsedMs > 0) frames * 1000.0 / elapsedMs else 0.0
    return BenchmarkResult(frames, elapsedMs, fps)
}
