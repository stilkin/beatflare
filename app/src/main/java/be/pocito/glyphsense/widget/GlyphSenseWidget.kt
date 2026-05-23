package be.pocito.glyphsense.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import be.pocito.glyphsense.MainActivity
import be.pocito.glyphsense.R

/**
 * Home screen widget: a single tap opens the app straight into the Beacon overlay,
 * rendered with the user's currently persisted Beacon settings (hue, text, colour,
 * react-to-sound). It is a stateless launcher — it does not start or stop the
 * persistent visualizer service and reflects no running/stopped state; the Beacon is
 * dismissed on the screen (back / tap), not via the widget.
 */
class GlyphSenseWidget : AppWidgetProvider() {

    companion object {
        // Distinct from the service notification's PendingIntent request codes (0 = content,
        // 1 = stop), both also getActivity → MainActivity. A shared request code makes the
        // PendingIntents filterEquals-equal, so the notification's FLAG_UPDATE_CURRENT would
        // overwrite our intent and strip EXTRA_LAUNCH_BEACON — silently breaking the launch.
        private const val REQUEST_LAUNCH_BEACON = 100
    }

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray,
    ) {
        for (id in widgetIds) updateWidget(context, manager, id)
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Click → open MainActivity directly into the Beacon overlay. singleTop +
        // SINGLE_TOP routes a tap while we're already alive through onNewIntent.
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_LAUNCH_BEACON, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context, REQUEST_LAUNCH_BEACON, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)

        manager.updateAppWidget(widgetId, views)
    }
}
