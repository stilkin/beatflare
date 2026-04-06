package be.pocito.glyphsense.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import be.pocito.glyphsense.MainActivity
import be.pocito.glyphsense.R
import be.pocito.glyphsense.audio.AudioAnalysis
import be.pocito.glyphsense.audio.AudioAnalyzer
import be.pocito.glyphsense.audio.AudioCapture
import be.pocito.glyphsense.glyph.GlyphController
import be.pocito.glyphsense.glyph.GlyphDriver
import be.pocito.glyphsense.model.DeviceProfile
import be.pocito.glyphsense.model.SettingsStore
import be.pocito.glyphsense.model.VisualizerSettings
import be.pocito.glyphsense.widget.GlyphSenseWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Foreground service that owns the entire audio→analysis→glyphs pipeline.
 *
 * Runs independently of the Activity lifecycle — survives screen lock so the
 * glyphs keep pulsing even when the phone is in your pocket at a festival.
 *
 * Start with [intentStart], stop with [intentStop]. The UI observes
 * [isRunning] and [analysisFlow] to render its display.
 */
class GlyphSenseService : Service() {

    companion object {
        private const val TAG = "GlyphSenseService"
        private const val CHANNEL_ID = "glyphsense_foreground"
        private const val CHANNEL_NAME = "BeatFlare visualizer"
        private const val NOTIF_ID = 1001

        private const val ACTION_START = "be.pocito.glyphsense.action.START"
        private const val ACTION_STOP = "be.pocito.glyphsense.action.STOP"

        // State observable by any UI/widget that wants to reflect running state.
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        // Live analysis stream for the UI to observe.
        private val _analysisFlow = MutableSharedFlow<AudioAnalysis>(
            replay = 0,
            extraBufferCapacity = 4,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val analysisFlow: SharedFlow<AudioAnalysis> = _analysisFlow.asSharedFlow()

        // Settings controllable from the UI at runtime.
        private val _settings = MutableStateFlow(VisualizerSettings())
        val settings: StateFlow<VisualizerSettings> = _settings.asStateFlow()

        private var appContext: Context? = null

        fun updateSettings(block: (VisualizerSettings) -> VisualizerSettings) {
            _settings.update(block)
            appContext?.let { SettingsStore.save(it, _settings.value) }
        }

        /** Load persisted settings without requiring the service to be running.
         *  Called from main thread only (LaunchedEffect in MainScreen, onCreate in service). */
        fun loadSettingsIfNeeded(context: Context) {
            if (appContext == null) {
                appContext = context.applicationContext
                _settings.value = SettingsStore.load(context.applicationContext)
            }
        }

        val isNothingDevice: Boolean = GlyphController.isNothingDevice()

        fun intentStart(context: Context): Intent =
            Intent(context, GlyphSenseService::class.java).setAction(ACTION_START)

        fun intentStop(context: Context): Intent =
            Intent(context, GlyphSenseService::class.java).setAction(ACTION_STOP)
    }

    private var controller: GlyphController? = null
    private lateinit var capture: AudioCapture
    private lateinit var analyzer: AudioAnalyzer
    private lateinit var driver: GlyphDriver
    private var deviceProfile: DeviceProfile? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pipelineJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate (nothingDevice=$isNothingDevice)")
        appContext = applicationContext
        _settings.value = SettingsStore.load(applicationContext)
        deviceProfile = DeviceProfile.detect()
        Log.d(TAG, "Device profile: ${deviceProfile?.name ?: "non-Nothing"}")
        if (isNothingDevice) controller = GlyphController(applicationContext)
        capture = AudioCapture()
        val spectrumBands = deviceProfile?.spectrumBands ?: 20
        analyzer = AudioAnalyzer(spectrumBands = spectrumBands)
        driver = GlyphDriver(deviceProfile ?: DeviceProfile.PHONE_3A)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_STOP -> {
                stopPipeline()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> { // ACTION_START or no action → start
                startAsForeground()
                startPipeline()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopPipeline()
        controller?.release()
        _isRunning.value = false
        scope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    // ─────────────────────────── Pipeline ───────────────────────────

    private fun startPipeline() {
        if (pipelineJob != null) return // already running
        controller?.init(
            onReady = { Log.d(TAG, "Glyph session open") },
            onError = { e -> Log.e(TAG, "Glyph init failed: $e") },
        )
        capture.start()
        if (!capture.isRunning()) {
            Log.e(TAG, "Mic capture failed to start")
            stopSelf()
            return
        }
        _isRunning.value = true
        GlyphSenseWidget.notifyStateChanged(applicationContext)
        pipelineJob = scope.launch {
            try {
                capture.buffers.collect { buf ->
                    val analysis = analyzer.process(buf)
                    _analysisFlow.tryEmit(analysis)
                    controller?.setFrameColors(driver.render(analysis, _settings.value))
                }
            } catch (e: Exception) {
                Log.e(TAG, "pipeline error: ${e.message}", e)
            }
        }
    }

    private fun stopPipeline() {
        pipelineJob?.cancel()
        pipelineJob = null
        capture.stop()
        controller?.setFrameColors(driver.blankFrame())
        _isRunning.value = false
        GlyphSenseWidget.notifyStateChanged(applicationContext)
    }

    // ─────────────────────────── Notification ───────────────────────────

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "BeatFlare audio visualizer running"
            setShowBadge(false)
        }
        mgr.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopPi = PendingIntent.getService(
            this, 1, intentStop(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("BeatFlare")
            .setContentText("Visualizing audio on glyphs")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPi)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPi)
            .build()
    }
}
