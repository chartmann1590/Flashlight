package com.charles.flashlight.data.feedback

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import com.charles.flashlight.BuildConfig
import com.charles.flashlight.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DiagnosticsHelper {
    fun collectMarkdown(context: Context): String {
        val appName = context.getString(R.string.app_name)
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(ActivityManager::class.java)
        activityManager?.getMemoryInfo(memoryInfo)
        val storage = StatFs(context.filesDir.absolutePath)
        val freeStorage = storage.availableBytes
        val totalStorage = storage.totalBytes
        val locale = Locale.getDefault().toString()
        val timeZone = TimeZone.getDefault().id
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

        return buildString {
            appendLine("## Diagnostics")
            appendLine()
            appendLine("- App: $appName")
            appendLine("- Package: ${BuildConfig.APPLICATION_ID}")
            appendLine("- Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("- Device: ${Build.BRAND} ${Build.MODEL}")
            appendLine("- Manufacturer: ${Build.MANUFACTURER}")
            appendLine("- Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
            appendLine("- Locale: $locale")
            appendLine("- Time Zone: $timeZone")
            appendLine("- Storage Free/Total: ${formatBytes(freeStorage)} / ${formatBytes(totalStorage)}")
            appendLine("- Memory Free/Total: ${formatBytes(memoryInfo.availMem)} / ${formatBytes(memoryInfo.totalMem)}")
            appendLine("- Timestamp: $timestamp")
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = listOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var index = 0
        while (value >= 1024 && index < units.lastIndex) {
            value /= 1024
            index++
        }
        return "${DecimalFormat("#.#").format(value)} ${units[index]}"
    }
}
