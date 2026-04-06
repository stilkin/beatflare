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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.pocito.glyphsense.model.VisualizerSettings
import be.pocito.glyphsense.service.GlyphSenseService
import be.pocito.glyphsense.ui.PartyOverlay
import be.pocito.glyphsense.ui.theme.BeatFlareMagenta
import be.pocito.glyphsense.ui.theme.BeatFlareOnSurfaceDim
import be.pocito.glyphsense.ui.theme.BeatFlareOrange
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
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                    ) { innerPadding ->
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Header ──
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = "BeatFlare",
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "BeatFlare",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                color = BeatFlareOnSurfaceDim,
            )
            Spacer(Modifier.height(4.dp))
            StatusDot(isRunning)
        }

        // ── Visualizer card (hero) ──
        VisualizerCard(
            spectrum = spectrum,
            bassLevel = bassLevel,
            beatFlash = beatFlash,
            isRunning = isRunning,
        )

        // ── Permissions (only if needed) ──
        if (!micGranted) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) { Text("Grant mic permission") }
        }
        if (!notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) { Text("Grant notification permission") }
        }

        // ── Start / Stop button ──
        GradientButton(
            text = if (isRunning) "Stop Visualizer" else "Start Visualizer",
            enabled = canStart,
            isActive = isRunning,
            onClick = {
                if (isRunning) context.startService(GlyphSenseService.intentStop(context))
                else context.startForegroundService(GlyphSenseService.intentStart(context))
            },
        )

        // ── Party mode button ──
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isRunning,
            onClick = onPartyMode,
            colors = ButtonDefaults.buttonColors(
                containerColor = BeatFlareOrange,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Party Mode", fontWeight = FontWeight.SemiBold)
        }

        // ── Settings card ──
        SettingsCard(
            settings = settings,
            onSettingsChange = { new -> GlyphSenseService.updateSettings { new } },
        )

        // ── Debug (collapsed) ──
        if (isRunning) {
            DebugSection(bassRaw, bassFloor, bassPeak)
        }

        if (!canStart) {
            Text(
                "Grant both permissions above to start.",
                style = MaterialTheme.typography.bodySmall,
                color = BeatFlareOnSurfaceDim,
            )
        }
    }
}

// ─────────────────── Status dot ───────────────────

@Composable
private fun StatusDot(isRunning: Boolean) {
    val color = if (isRunning) Color(0xFF4CAF50) else BeatFlareOnSurfaceDim
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            if (isRunning) "Running" else "Stopped",
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

// ─────────────────── Visualizer card ───────────────────

@Composable
private fun VisualizerCard(
    spectrum: FloatArray,
    bassLevel: Float,
    beatFlash: Int,
    isRunning: Boolean,
) {
    val beatAlpha = if (beatFlash > 0) 0.15f else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (beatAlpha > 0f) Modifier.background(BeatFlareOrange.copy(alpha = beatAlpha))
                    else Modifier
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Spectrum bars — the hero
                GradientSpectrumBars(
                    values = spectrum,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )

                // Bass bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "BASS",
                        style = MaterialTheme.typography.labelSmall,
                        color = BeatFlareOnSurfaceDim,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bassLevel.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BeatFlareMagenta, BeatFlareOrange)
                                    )
                                ),
                        )
                    }
                }

                if (!isRunning) {
                    Text(
                        "Start the visualizer to see audio analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = BeatFlareOnSurfaceDim,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientSpectrumBars(values: FloatArray, modifier: Modifier = Modifier) {
    val magenta = BeatFlareMagenta
    val orange = BeatFlareOrange

    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val barWidth = w / values.size
        val gap = barWidth * 0.12f
        val cornerRadius = barWidth * 0.2f

        for (i in values.indices) {
            val v = values[i].coerceIn(0f, 1f)
            val barH = h * v
            if (barH < 1f) continue

            // Gradient color: magenta (left) → orange (right)
            val fraction = i.toFloat() / (values.size - 1).coerceAtLeast(1)
            val barColor = lerp(magenta, orange, fraction)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(i * barWidth + gap / 2f, h - barH),
                size = Size(barWidth - gap, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
            )
        }
    }
}

// ─────────────────── Gradient button ───────────────────

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    if (enabled) {
        val gradient = Brush.horizontalGradient(
            if (isActive) listOf(BeatFlareMagenta, BeatFlareOrange)
            else listOf(BeatFlareMagenta.copy(alpha = 0.8f), BeatFlareOrange.copy(alpha = 0.8f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(gradient)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = false,
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(text)
        }
    }
}

// ─────────────────── Settings card ───────────────────

@Composable
private fun SettingsCard(
    settings: VisualizerSettings,
    onSettingsChange: (VisualizerSettings) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.titleSmall,
                color = BeatFlareOnSurfaceDim,
            )

            // Brightness
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Brightness",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(90.dp),
                )
                Slider(
                    value = settings.brightness,
                    onValueChange = { onSettingsChange(settings.copy(brightness = it)) },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = BeatFlareMagenta,
                        activeTrackColor = BeatFlareMagenta,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
                Text(
                    "${(settings.brightness * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeatFlareOnSurfaceDim,
                    modifier = Modifier.width(36.dp),
                )
            }

            // Zone toggles
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
    }
}

@Composable
private fun ZoneToggle(label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BeatFlareMagenta,
                uncheckedThumbColor = BeatFlareOnSurfaceDim,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else BeatFlareOnSurfaceDim,
        )
    }
}

// ─────────────────── Debug section ───────────────────

@Composable
private fun DebugSection(bassRaw: Float, bassFloor: Float, bassPeak: Float) {
    var expanded by remember { mutableStateOf(false) }
    TextButton(onClick = { expanded = !expanded }) {
        Text(
            if (expanded) "▾ Debug" else "▸ Debug",
            color = BeatFlareOnSurfaceDim,
        )
    }
    if (expanded) {
        Text(
            "log raw=${"%.1f".format(bassRaw)}  floor=${"%.1f".format(bassFloor)}  peak=${"%.1f".format(bassPeak)}",
            style = MaterialTheme.typography.bodySmall,
            color = BeatFlareOnSurfaceDim,
        )
    }
}
