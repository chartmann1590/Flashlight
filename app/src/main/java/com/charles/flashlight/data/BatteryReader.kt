package com.charles.flashlight.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * One-shot battery reads from the sticky ACTION_BATTERY_CHANGED intent.
 * No permission and no registered receiver: the level is read only when the
 * Home screen is shown or the torch is toggled — never polled in background.
 */
object BatteryReader {

    data class BatteryInfo(val percent: Int, val isCharging: Boolean)

    fun read(context: Context): BatteryInfo? {
        val intent: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent == null) return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return BatteryInfo(percent = level * 100 / scale, isCharging = charging)
    }

    /** Below this level (and not charging) the Home screen warns before torch use. */
    const val LOW_BATTERY_PERCENT = 20
}
