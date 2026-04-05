package be.pocito.glyphsense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
                    SpikeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Spike UI: test Glyph SDK lifecycle, per-LED brightness, refresh rate, and mic capture.
 */
@Composable
fun SpikeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { GlyphController(context) }
    val capture = remember { AudioCapture() }

    var sessionOpen by remember { mutableStateOf(false) }
    val logLines = remember { mutableStateOf(listOf<String>()) }

    var micPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var micRunning by remember { mutableStateOf(false) }
    var micPeak by remember { mutableIntStateOf(0) } // 0..32767
    var micPeakPct by remember { mutableIntStateOf(0) } // 0..100

    val analyzer = remember { AudioAnalyzer() }
    val driver = remember { GlyphDriver() }
    var drivingGlyphs by remember { mutableStateOf(false) }
    var bassLevel by remember { mutableStateOf(0f) }
    var bassRaw by remember { mutableStateOf(0f) }
    var bassFloor by remember { mutableStateOf(0f) }
    var bassPeak by remember { mutableStateOf(0f) }
    var spectrum by remember { mutableStateOf(FloatArray(20)) }
    var beatFlash by remember { mutableIntStateOf(0) } // frames since last beat

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        logLines.value = (listOf(
            "[$ts] RECORD_AUDIO permission " + if (granted) "granted" else "DENIED"
        ) + logLines.value).take(200)
    }

    fun log(line: String) {
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        logLines.value = (listOf("[$ts] $line") + logLines.value).take(200)
    }

    DisposableEffect(Unit) {
        log("Device: ${controller.deviceName()}")
        onDispose {
            capture.stop()
            controller.release()
        }
    }

    // Observe mic buffers when capturing: peak amplitude + FFT analysis
    LaunchedEffect(micRunning) {
        if (micRunning) {
            try {
                capture.buffers.collect { buf ->
                    val p = buf.peakAmplitude()
                    micPeak = p
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
                android.util.Log.e("SpikeScreen", "collect error: ${e.message}", e)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Glyph SDK Spike", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Session: ${if (sessionOpen) "OPEN" else "closed"}",
            style = MaterialTheme.typography.bodyMedium,
        )

        HorizontalDivider()

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !sessionOpen,
            onClick = {
                log("init() + openSession() ...")
                controller.init(
                    onReady = { sessionOpen = true; log("Session OPEN") },
                    onError = { err -> log("ERROR: $err") },
                )
            },
        ) { Text("Init + Open Session") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                controller.release()
                sessionOpen = false
                log("Released")
            },
        ) { Text("Release Session") }

        HorizontalDivider()
        Text("LED Tests", style = MaterialTheme.typography.titleMedium)

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A)
                arr[20] = 4095
                controller.setFrameColors(arr)
                log("Single LED (A_1, idx 20) @ 4095")
            },
        ) { Text("Single LED on (A_1)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A) { 4095 }
                controller.setFrameColors(arr)
                log("All LEDs @ 4095")
            },
        ) { Text("All LEDs full") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A) { i ->
                    (i * 4095 / (LED_COUNT_PHONE_3A - 1))
                }
                controller.setFrameColors(arr)
                log("Gradient 0..4095 across 36 LEDs")
            },
        ) { Text("Gradient (brightness test)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A) { 255 }
                controller.setFrameColors(arr)
                log("All LEDs @ 255 (low test)")
            },
        ) { Text("All LEDs @ 255 (low)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                controller.turnOff()
                log("turnOff()")
            },
        ) { Text("Turn Off") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A)
                for (i in 0..19) arr[i] = 4095
                controller.setFrameColors(arr)
                log("Zone C only (idx 0..19) @ 4095")
            },
        ) { Text("Only zone C (20 LEDs)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A)
                for (i in 20..30) arr[i] = 4095
                controller.setFrameColors(arr)
                log("Zone A only (idx 20..30) @ 4095")
            },
        ) { Text("Only zone A (11 LEDs)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                val arr = IntArray(LED_COUNT_PHONE_3A)
                for (i in 31..35) arr[i] = 4095
                controller.setFrameColors(arr)
                log("Zone B only (idx 31..35) @ 4095")
            },
        ) { Text("Only zone B (5 LEDs)") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                scope.launch {
                    log("Walk: lighting idx 0..35, 400ms each")
                    val arr = IntArray(LED_COUNT_PHONE_3A)
                    for (idx in 0 until LED_COUNT_PHONE_3A) {
                        for (i in arr.indices) arr[i] = 0
                        arr[idx] = 4095
                        controller.setFrameColors(arr)
                        log("  lit idx $idx")
                        kotlinx.coroutines.delay(400)
                    }
                    controller.turnOff()
                    log("Walk complete")
                }
            },
        ) { Text("Walk LEDs (slow chase)") }

        HorizontalDivider()
        Text("Benchmark", style = MaterialTheme.typography.titleMedium)

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = sessionOpen,
            onClick = {
                scope.launch {
                    log("Benchmark: running 3s loop...")
                    val result = withContext(Dispatchers.Default) {
                        benchmarkRefreshRate(controller, durationMs = 3000L)
                    }
                    log(
                        "Bench: ${result.frames} frames in ${result.elapsedMs}ms " +
                            "= ${"%.1f".format(result.fps)} fps"
                    )
                    controller.turnOff()
                }
            },
        ) { Text("Benchmark refresh rate (3s)") }

        HorizontalDivider()
        Text("Microphone", style = MaterialTheme.typography.titleMedium)
        Text(
            "Permission: " + if (micPermissionGranted) "granted" else "NOT granted",
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !micPermissionGranted,
            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        ) { Text("Request mic permission") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = micPermissionGranted && !micRunning,
            onClick = {
                capture.start()
                micRunning = capture.isRunning()
                log(if (micRunning) "Mic capture started" else "Mic capture FAILED to start")
            },
        ) { Text("Start mic capture") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = micRunning,
            onClick = {
                capture.stop()
                micRunning = false
                micPeak = 0
                micPeakPct = 0
                log("Mic capture stopped")
            },
        ) { Text("Stop mic capture") }

        if (micRunning) {
            Text(
                "Peak: $micPeak / 32767  ($micPeakPct%)",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { micPeakPct / 100f },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()
            Text("Analysis", style = MaterialTheme.typography.titleMedium)

            Text(
                "Bass: ${"%.2f".format(bassLevel)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { bassLevel },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "  log: raw=${"%.1f".format(bassRaw)}  floor=${"%.1f".format(bassFloor)}  peak=${"%.1f".format(bassPeak)}",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Beat: ",
                    style = MaterialTheme.typography.bodyMedium,
                )
                val beatColor = if (beatFlash > 0) Color.Red else Color.Gray
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                        .background(beatColor),
                )
            }

            Text("Spectrum (20 bands)", style = MaterialTheme.typography.bodyMedium)
            SpectrumBars(
                values = spectrum,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            )

            HorizontalDivider()
            Text("Drive Glyphs", style = MaterialTheme.typography.titleMedium)
            Text(
                if (!sessionOpen) "Open a glyph session first"
                else if (drivingGlyphs) "LIVE — audio driving glyphs"
                else "Not driving glyphs",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = sessionOpen && !drivingGlyphs,
                onClick = {
                    drivingGlyphs = true
                    log("Glyph driving: ON")
                },
            ) { Text("Drive glyphs from audio") }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = drivingGlyphs,
                onClick = {
                    drivingGlyphs = false
                    controller.setFrameColors(driver.blankFrame())
                    log("Glyph driving: OFF")
                },
            ) { Text("Stop driving glyphs") }
        }

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

/**
 * Calls setFrameColors in a tight loop for [durationMs] milliseconds,
 * animating a single-LED "runner" so we visually confirm something's happening.
 */
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
