package be.pocito.glyphsense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import be.pocito.glyphsense.model.VisualizerSettings
import be.pocito.glyphsense.service.GlyphSenseService
import be.pocito.glyphsense.ui.PartyOverlay
import be.pocito.glyphsense.ui.theme.GlyphSenseTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlyphSenseTheme {
                var partyMode by remember { mutableStateOf(false) }
                Box(Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            onPartyMode = { partyMode = true },
                        )
                    }
                    if (partyMode) {
                        PartyOverlay(onDismiss = { partyMode = false })
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, onPartyMode: () -> Unit = {}) {
    val context = LocalContext.current

    // Permissions
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { micGranted = it }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = it }

    // Service state
    val isRunning by GlyphSenseService.isRunning.collectAsState()
    val settings by GlyphSenseService.settings.collectAsState()

    // Live analysis
    var bassLevel by remember { mutableStateOf(0f) }
    var spectrum by remember { mutableStateOf(FloatArray(20)) }
    var beatFlash by remember { mutableIntStateOf(0) }
    var bassRaw by remember { mutableStateOf(0f) }
    var bassFloor by remember { mutableStateOf(0f) }
    var bassPeak by remember { mutableStateOf(0f) }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            bassLevel = 0f; spectrum = FloatArray(20); beatFlash = 0
            return@LaunchedEffect
        }
        GlyphSenseService.analysisFlow.collect { a ->
            bassLevel = a.bassLevel
            bassRaw = a.bassRaw
            bassFloor = a.bassFloor
            bassPeak = a.bassPeak
            spectrum = a.spectrum
            beatFlash = if (a.beat) 3 else (beatFlash - 1).coerceAtLeast(0)
        }
    }

    val canStart = micGranted && notifGranted

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("BeatFlare", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Visualizer: ${if (isRunning) "RUNNING" else "stopped"}",
            style = MaterialTheme.typography.bodyMedium,
        )

        HorizontalDivider()

        // ── Permissions ──
        if (!micGranted) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            ) { Text("Grant mic permission") }
        }
        if (!notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            ) { Text("Grant notification permission") }
        }

        // ── Start / Stop ──
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = canStart,
            onClick = {
                if (isRunning) context.startService(GlyphSenseService.intentStop(context))
                else context.startForegroundService(GlyphSenseService.intentStart(context))
            },
        ) { Text(if (isRunning) "Stop visualizer" else "Start visualizer") }

        if (!canStart) {
            Text("Grant both permissions above to start.",
                style = MaterialTheme.typography.bodySmall)
        }

        // ── Settings ──
        HorizontalDivider()
        SettingsPanel(
            settings = settings,
            onSettingsChange = { new -> GlyphSenseService.updateSettings { new } },
        )

        // ── Party mode ──
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isRunning,
            onClick = onPartyMode,
        ) { Text("Party mode") }

        // ── Analysis ──
        if (isRunning) {
            HorizontalDivider()
            Text("Analysis", style = MaterialTheme.typography.titleMedium)
            AnalysisDisplay(
                bassLevel = bassLevel,
                bassRaw = bassRaw,
                bassFloor = bassFloor,
                bassPeak = bassPeak,
                beatFlash = beatFlash,
                spectrum = spectrum,
            )
        }
    }
}

// ─────────────────── Settings panel ───────────────────

@Composable
private fun SettingsPanel(
    settings: VisualizerSettings,
    onSettingsChange: (VisualizerSettings) -> Unit,
) {
    Text("Settings", style = MaterialTheme.typography.titleMedium)

    Text("Brightness: ${(settings.brightness * 100).roundToInt()}%",
        style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = settings.brightness,
        onValueChange = { onSettingsChange(settings.copy(brightness = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ZoneToggle("Spectrum", settings.zoneCEnabled) {
            onSettingsChange(settings.copy(zoneCEnabled = it))
        }
        ZoneToggle("Bass", settings.zoneAEnabled) {
            onSettingsChange(settings.copy(zoneAEnabled = it))
        }
        ZoneToggle("Beat", settings.zoneBEnabled) {
            onSettingsChange(settings.copy(zoneBEnabled = it))
        }
    }
}

@Composable
private fun ZoneToggle(label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Switch(checked = enabled, onCheckedChange = onToggle)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

// ─────────────────── Analysis display ───────────────────

@Composable
private fun AnalysisDisplay(
    bassLevel: Float,
    bassRaw: Float,
    bassFloor: Float,
    bassPeak: Float,
    beatFlash: Int,
    spectrum: FloatArray,
) {
    Text("Bass: ${"%.2f".format(bassLevel)}", style = MaterialTheme.typography.bodyMedium)
    LinearProgressIndicator(progress = { bassLevel }, modifier = Modifier.fillMaxWidth())
    Text(
        "  log raw=${"%.1f".format(bassRaw)}  floor=${"%.1f".format(bassFloor)}  peak=${"%.1f".format(bassPeak)}",
        style = MaterialTheme.typography.bodySmall,
    )

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Beat: ", style = MaterialTheme.typography.bodyMedium)
        val beatColor = if (beatFlash > 0) Color.Red else Color.Gray
        Box(Modifier.height(24.dp).fillMaxWidth().background(beatColor))
    }

    Text("Spectrum", style = MaterialTheme.typography.bodyMedium)
    SpectrumBars(values = spectrum, modifier = Modifier.fillMaxWidth().height(80.dp))
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
