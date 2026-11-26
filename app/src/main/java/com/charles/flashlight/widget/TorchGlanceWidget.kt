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

class TorchGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
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
                    com.charles.flashlight.torch.TorchController.initialize(context)
                    val isOn = com.charles.flashlight.torch.TorchController.isActive.value
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
