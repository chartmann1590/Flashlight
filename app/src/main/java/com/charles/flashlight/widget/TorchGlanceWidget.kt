package com.charles.flashlight.widget

import android.content.Context
import android.content.Intent
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import com.charles.flashlight.MainActivity
import com.charles.flashlight.R

class TorchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TorchGlanceWidget()
}

/** Refresh widget instances and the QS tile whenever torch state changes. */
object TorchWidgetUpdater {
    suspend fun updateWidgets(context: Context) {
        runCatching {
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(TorchGlanceWidget::class.java)
            ids.forEach { TorchGlanceWidget().update(context, it) }
        }
    }

    fun requestTileRefresh(context: Context) {
        runCatching {
            android.service.quicksettings.TileService.requestListeningState(
                context,
                android.content.ComponentName(
                    context,
                    com.charles.flashlight.torch.TorchTileService::class.java
                )
            )
        }
    }
}

class TorchGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read torch state here (not inside the composition) so lint's
        // StateFlowValueCalledInComposition check passes; state changes
        // trigger a fresh provideGlance via TorchWidgetUpdater.
        com.charles.flashlight.torch.TorchController.initialize(context)
        val isOn = com.charles.flashlight.torch.TorchController.isActive.value
        provideContent {
            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(MainActivity.EXTRA_FROM_WIDGET_TOGGLE, true)
                }
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = context.getString(R.string.widget_title))
                    Text(
                        text = context.getString(
                            if (isOn) R.string.widget_state_on else R.string.widget_state_off
                        )
                    )
                    Button(
                        text = context.getString(R.string.widget_toggle),
                        onClick = actionStartActivity(intent)
                    )
                }
            }
        }
    }
}
