package be.pocito.glyphsense.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import be.pocito.glyphsense.R
import be.pocito.glyphsense.service.GlyphSenseService

/**
 * Home screen widget: single tap to start/stop the GlyphSense visualizer.
 *
 * Shows a dark rounded rectangle with "GlyphSense" + status text.
 * Background turns green when running, dark when stopped.
 *
 * The widget is updated in two ways:
 *  1. [onUpdate] — called by the system on widget creation / periodic refresh
 *  2. [ACTION_STATE_CHANGED] broadcast — sent by [GlyphSenseService] when it starts/stops
 */
class GlyphSenseWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE = "be.pocito.glyphsense.widget.TOGGLE"
        const val ACTION_STATE_CHANGED = "be.pocito.glyphsense.widget.STATE_CHANGED"

        /** Call from the service to push a visual refresh to all widget instances. */
        fun notifyStateChanged(context: Context) {
            val intent = Intent(context, GlyphSenseWidget::class.java).apply {
                action = ACTION_STATE_CHANGED
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray,
    ) {
        for (id in widgetIds) updateWidget(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE -> handleToggle(context)
            ACTION_STATE_CHANGED -> refreshAll(context)
        }
    }

    private fun handleToggle(context: Context) {
        // The widget is the persistent consumer's other owner (alongside the Lights
        // Start/Stop). The service broadcasts STATE_CHANGED on the real transition → refreshAll.
        if (GlyphSenseService.isRunning.value) {
            GlyphSenseService.release(context, GlyphSenseService.Consumer.PERSISTENT)
        } else {
            GlyphSenseService.acquire(context, GlyphSenseService.Consumer.PERSISTENT)
        }
    }

    private fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, GlyphSenseWidget::class.java)
        )
        for (id in ids) updateWidget(context, manager, id)
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
    ) {
        val running = GlyphSenseService.isRunning.value
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Visual state
        val bgRes = if (running) R.drawable.widget_bg_running else R.drawable.widget_bg_stopped
        val statusText = if (running) "Running — tap to stop" else "Tap to start"
        views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)
        views.setTextViewText(R.id.widget_status, statusText)

        // Click → toggle
        val toggleIntent = Intent(context, GlyphSenseWidget::class.java).apply {
            action = ACTION_TOGGLE
        }
        val pi = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)

        manager.updateAppWidget(widgetId, views)
    }
}
