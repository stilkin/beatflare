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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.pocito.glyphsense.model.BeaconTextColor
import be.pocito.glyphsense.model.PartyTheme
import be.pocito.glyphsense.model.VisualizerSettings
import be.pocito.glyphsense.service.GlyphSenseService
import be.pocito.glyphsense.ui.BeaconHuePicker
import be.pocito.glyphsense.ui.BeaconOverlay
import be.pocito.glyphsense.ui.EmojiOverlaySettings
import be.pocito.glyphsense.ui.PartyOverlay
import be.pocito.glyphsense.ui.theme.BeatFlareMagenta
import be.pocito.glyphsense.ui.theme.BeatFlareOnSurfaceDim
import be.pocito.glyphsense.ui.theme.BeatFlareOrange
import be.pocito.glyphsense.ui.theme.GlyphSenseTheme
import kotlin.math.roundToInt

private enum class Tab { Beacon, Play, Show, Glyphs }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlyphSenseTheme {
                var partyMode by remember { mutableStateOf(false) }
                var showBeacon by remember { mutableStateOf(false) }
                Box(Modifier.fillMaxSize()) {
                    MainScreen(
                        onLaunchParty = {
                            showBeacon = false
                            partyMode = true
                        },
                        onStopParty = { partyMode = false },
                        onLaunchBeacon = {
                            partyMode = false
                            showBeacon = true
                        },
                    )
                    // Mutually exclusive overlays — Beacon wins if both flags somehow get set.
                    if (showBeacon) {
                        BeaconOverlay(onDismiss = { showBeacon = false })
                    } else if (partyMode) {
                        PartyOverlay(onDismiss = { partyMode = false })
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onLaunchParty: () -> Unit = {},
    onStopParty: () -> Unit = {},
    onLaunchBeacon: () -> Unit = {},
) {
    val context = LocalContext.current
    val isNothingDevice = GlyphSenseService.isNothingDevice

    LaunchedEffect(Unit) { GlyphSenseService.loadSettingsIfNeeded(context) }

    var selectedTab by rememberSaveable { mutableStateOf(Tab.Play) }
    // Glyphs tab disappears on non-Nothing devices — fall back gracefully.
    if (selectedTab == Tab.Glyphs && !isNothingDevice) selectedTab = Tab.Play

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNav(
                selected = selectedTab,
                showGlyphs = isNothingDevice,
                onSelect = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        val tabModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (selectedTab) {
            Tab.Beacon -> BeaconTab(tabModifier, onLaunchBeacon = onLaunchBeacon)
            Tab.Play -> PlayTab(
                modifier = tabModifier,
                isNothingDevice = isNothingDevice,
                onLaunchParty = onLaunchParty,
                onStopParty = onStopParty,
            )
            Tab.Show -> ShowTab(tabModifier)
            Tab.Glyphs -> GlyphsTab(tabModifier)
        }
    }
}

// ─────────────────── Bottom navigation ───────────────────

@Composable
private fun BottomNav(selected: Tab, showGlyphs: Boolean, onSelect: (Tab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = selected == Tab.Beacon,
            onClick = { onSelect(Tab.Beacon) },
            icon = { Text("★", fontSize = 20.sp) },
            label = { Text("Beacon") },
            colors = navColors(),
        )
        NavigationBarItem(
            selected = selected == Tab.Play,
            onClick = { onSelect(Tab.Play) },
            icon = { Text("▶", fontSize = 18.sp) },
            label = { Text("Play") },
            colors = navColors(),
        )
        NavigationBarItem(
            selected = selected == Tab.Show,
            onClick = { onSelect(Tab.Show) },
            icon = { Text("✦", fontSize = 20.sp) },
            label = { Text("Show") },
            colors = navColors(),
        )
        if (showGlyphs) {
            NavigationBarItem(
                selected = selected == Tab.Glyphs,
                onClick = { onSelect(Tab.Glyphs) },
                icon = { Text("✱", fontSize = 20.sp) },
                label = { Text("Glyphs") },
                colors = navColors(),
            )
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = BeatFlareMagenta,
    indicatorColor = BeatFlareMagenta,
    unselectedIconColor = BeatFlareOnSurfaceDim,
    unselectedTextColor = BeatFlareOnSurfaceDim,
)

// ─────────────────── Tabs ───────────────────

@Composable
private fun PlayTab(
    modifier: Modifier,
    isNothingDevice: Boolean,
    onLaunchParty: () -> Unit,
    onStopParty: () -> Unit,
) {
    val context = LocalContext.current
    val isRunning by GlyphSenseService.isRunning.collectAsState()
    val settings by GlyphSenseService.settings.collectAsState()

    // Permissions
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { micGranted = it }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { notifGranted = it }

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
    // On non-Nothing devices party mode is the only possible output; settings UI is hidden.
    val partyOnTap = if (isNothingDevice) settings.partyOutputEnabled else true

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TabHeader("▶", "Play", trailing = { StatusDot(isRunning) })

        VisualizerCard(
            spectrum = spectrum,
            bassLevel = bassLevel,
            beatFlash = beatFlash,
            isRunning = isRunning,
            // Tap to (re-)launch the party overlay while running, if that output is enabled.
            onTap = if (isRunning && partyOnTap) onLaunchParty else null,
        )

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

        // Output toggles — only meaningful on Nothing devices (where Glyphs is an option).
        if (isNothingDevice) {
            OutputToggles(
                glyphsEnabled = settings.glyphsOutputEnabled,
                partyEnabled = settings.partyOutputEnabled,
                onGlyphs = { newG ->
                    // Never let both toggles be off — auto-flip the other.
                    val newP = if (!newG && !settings.partyOutputEnabled) true else settings.partyOutputEnabled
                    GlyphSenseService.updateSettings {
                        it.copy(glyphsOutputEnabled = newG, partyOutputEnabled = newP)
                    }
                },
                onParty = { newP ->
                    val newG = if (!newP && !settings.glyphsOutputEnabled) true else settings.glyphsOutputEnabled
                    GlyphSenseService.updateSettings {
                        it.copy(glyphsOutputEnabled = newG, partyOutputEnabled = newP)
                    }
                },
            )
        }

        GradientButton(
            text = if (isRunning) "Stop Visualizer" else "Start Visualizer",
            enabled = canStart,
            isActive = isRunning,
            onClick = {
                if (isRunning) {
                    onStopParty()
                    context.startService(GlyphSenseService.intentStop(context))
                } else {
                    context.startForegroundService(GlyphSenseService.intentStart(context))
                    if (partyOnTap) onLaunchParty()
                }
            },
        )

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

@Composable
private fun OutputToggles(
    glyphsEnabled: Boolean,
    partyEnabled: Boolean,
    onGlyphs: (Boolean) -> Unit,
    onParty: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ToggleRow("Glyphs", glyphsEnabled, onGlyphs)
            ToggleRow("Show", partyEnabled, onParty)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BeatFlareMagenta,
                uncheckedThumbColor = BeatFlareOnSurfaceDim,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun ShowTab(modifier: Modifier) {
    val settings by GlyphSenseService.settings.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TabHeader("✦", "Show")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Theme",
                    style = MaterialTheme.typography.titleSmall,
                    color = BeatFlareOnSurfaceDim,
                )
                GroupedThemeList(
                    selected = settings.partyTheme,
                    onSelect = { t ->
                        GlyphSenseService.updateSettings { it.copy(partyTheme = t) }
                    },
                )
            }
        }
    }
}

@Composable
private fun BeaconTab(modifier: Modifier, onLaunchBeacon: () -> Unit) {
    val context = LocalContext.current
    val settings by GlyphSenseService.settings.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TabHeader("★", "Beacon")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Background",
                    style = MaterialTheme.typography.titleSmall,
                    color = BeatFlareOnSurfaceDim,
                )
                BeaconHuePicker(
                    hue = settings.beaconHue,
                    onHueChange = { h ->
                        GlyphSenseService.updateSettings { it.copy(beaconHue = h) }
                    },
                )
                ToggleRow(
                    label = "React to sound",
                    checked = settings.beaconReactToSound,
                    onChange = { v ->
                        GlyphSenseService.updateSettings { it.copy(beaconReactToSound = v) }
                    },
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EmojiOverlaySettings(
                    current = settings.beaconText,
                    onChange = { txt ->
                        GlyphSenseService.updateSettings { it.copy(beaconText = txt) }
                    },
                )
                Text(
                    "Text colour",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeatFlareOnSurfaceDim,
                )
                BeaconTextColorRow(
                    selected = settings.beaconTextColor,
                    onSelect = { c ->
                        GlyphSenseService.updateSettings { it.copy(beaconTextColor = c) }
                    },
                )
            }
        }

        GradientButton(
            text = "Light up beacon",
            enabled = true,
            isActive = true,
            onClick = {
                // Auto-start the service only when reactive Beacon needs audio.
                if (settings.beaconReactToSound && !GlyphSenseService.isRunning.value) {
                    context.startForegroundService(GlyphSenseService.intentStart(context))
                }
                onLaunchBeacon()
            },
        )
    }
}

@Composable
private fun BeaconTextColorRow(selected: BeaconTextColor, onSelect: (BeaconTextColor) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BeaconTextColor.entries.forEach { c ->
            val isSelected = c == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(c.color)
                    .clickable { onSelect(c) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(BeatFlareMagenta),
                    )
                }
            }
        }
    }
}

@Composable
private fun GlyphsTab(modifier: Modifier) {
    val settings by GlyphSenseService.settings.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TabHeader("✱", "Glyphs")

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
                    "Brightness",
                    style = MaterialTheme.typography.titleSmall,
                    color = BeatFlareOnSurfaceDim,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = settings.brightness.coerceIn(0.05f, 1f),
                        onValueChange = { v ->
                            GlyphSenseService.updateSettings { it.copy(brightness = v) }
                        },
                        valueRange = 0.05f..1f,
                        steps = 18,
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
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Zones",
                    style = MaterialTheme.typography.titleSmall,
                    color = BeatFlareOnSurfaceDim,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ZoneToggle("Spectrum", settings.zoneCEnabled) { v ->
                        GlyphSenseService.updateSettings { it.copy(zoneCEnabled = v) }
                    }
                    ZoneToggle("Bass", settings.zoneAEnabled) { v ->
                        GlyphSenseService.updateSettings { it.copy(zoneAEnabled = v) }
                    }
                    ZoneToggle("Beat", settings.zoneBEnabled) { v ->
                        GlyphSenseService.updateSettings { it.copy(zoneBEnabled = v) }
                    }
                }
            }
        }
    }
}

// ─────────────────── Tab header + status ───────────────────

@Composable
private fun TabHeader(
    icon: String,
    title: String,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            icon,
            fontSize = 22.sp,
            color = BeatFlareMagenta,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

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
    onTap: (() -> Unit)? = null,
) {
    val beatAlpha = if (beatFlash > 0) 0.15f else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (beatAlpha > 0f) {
                        Modifier.background(BeatFlareOrange.copy(alpha = beatAlpha))
                    } else {
                        Modifier
                    },
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GradientSpectrumBars(
                    values = spectrum,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )

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
                                        listOf(BeatFlareMagenta, BeatFlareOrange),
                                    ),
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
            if (isActive) {
                listOf(BeatFlareMagenta, BeatFlareOrange)
            } else {
                listOf(BeatFlareMagenta.copy(alpha = 0.8f), BeatFlareOrange.copy(alpha = 0.8f))
            },
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

// ─────────────────── Theme selector ───────────────────

private val themeGroups: List<Pair<String, List<PartyTheme>>> = listOf(
    "Spectrum" to listOf(PartyTheme.SPECTRUM, PartyTheme.RAINBOW),
    "Mood" to listOf(PartyTheme.FIRE, PartyTheme.OCEAN, PartyTheme.FOREST),
    "Pulse" to listOf(PartyTheme.BREATHE, PartyTheme.SWEEP, PartyTheme.STROBE),
)

private fun PartyTheme.subtitle(): String = when (this) {
    PartyTheme.SPECTRUM -> "Color follows the music's frequency"
    PartyTheme.RAINBOW -> "Hue cycles continuously, brightness from bass"
    PartyTheme.FIRE -> "Warm reds and oranges, intensity tracks bass"
    PartyTheme.OCEAN -> "Cool blues and teals, hue shifts with mids"
    PartyTheme.FOREST -> "Greens and yellows, hue shifts with mids"
    PartyTheme.BREATHE -> "Slow sine pulse on a fixed hue"
    PartyTheme.SWEEP -> "Slow hue rotation across cool tones"
    PartyTheme.STROBE -> "Bright white flash on every beat"
}

@Composable
private fun GroupedThemeList(selected: PartyTheme, onSelect: (PartyTheme) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        themeGroups.forEach { (label, themes) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = BeatFlareOnSurfaceDim,
                )
                themes.forEach { theme ->
                    ThemeRow(theme, theme == selected) { onSelect(theme) }
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(theme: PartyTheme, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) {
                    BeatFlareMagenta.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left-edge stripe in the theme's signature colour(s). Flush to the card edge.
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(theme.stripeBrush()),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                theme.label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) BeatFlareMagenta else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                theme.subtitle(),
                style = MaterialTheme.typography.bodySmall,
                color = BeatFlareOnSurfaceDim,
            )
        }
        if (isSelected) {
            Text(
                "✓",
                style = MaterialTheme.typography.bodyLarge,
                color = BeatFlareMagenta,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
    }
}

private fun PartyTheme.stripeBrush(): Brush = when (this) {
    PartyTheme.SPECTRUM -> Brush.verticalGradient(
        listOf(
            Color.hsl(0f, 0.85f, 0.5f),
            Color.hsl(70f, 0.85f, 0.5f),
            Color.hsl(140f, 0.85f, 0.5f),
            Color.hsl(210f, 0.85f, 0.5f),
            Color.hsl(280f, 0.85f, 0.5f),
        ),
    )
    PartyTheme.RAINBOW -> Brush.verticalGradient(
        listOf(
            Color.hsl(0f, 0.9f, 0.55f),
            Color.hsl(60f, 0.9f, 0.55f),
            Color.hsl(120f, 0.9f, 0.55f),
            Color.hsl(180f, 0.9f, 0.55f),
            Color.hsl(240f, 0.9f, 0.55f),
            Color.hsl(300f, 0.9f, 0.55f),
            Color.hsl(360f, 0.9f, 0.55f),
        ),
    )
    PartyTheme.FIRE -> Brush.verticalGradient(
        listOf(
            Color.hsl(10f, 0.95f, 0.5f),
            Color.hsl(30f, 0.95f, 0.55f),
            Color.hsl(50f, 0.95f, 0.55f),
        ),
    )
    PartyTheme.OCEAN -> Brush.verticalGradient(
        listOf(
            Color.hsl(200f, 0.80f, 0.40f),
            Color.hsl(180f, 0.80f, 0.45f),
            Color.hsl(160f, 0.80f, 0.50f),
        ),
    )
    PartyTheme.FOREST -> Brush.verticalGradient(
        listOf(
            Color.hsl(90f, 0.75f, 0.45f),
            Color.hsl(115f, 0.75f, 0.40f),
            Color.hsl(140f, 0.75f, 0.35f),
        ),
    )
    PartyTheme.BREATHE -> SolidColor(Color.hsl(280f, 0.70f, 0.50f))
    PartyTheme.SWEEP -> Brush.verticalGradient(
        listOf(
            Color.hsl(200f, 0.75f, 0.50f),
            Color.hsl(260f, 0.75f, 0.50f),
            Color.hsl(320f, 0.75f, 0.50f),
        ),
    )
    PartyTheme.STROBE -> SolidColor(Color.White)
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

// ─────────────────── Debug ───────────────────

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
