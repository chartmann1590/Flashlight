package com.charles.flashlight.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : DefaultLifecycleObserver, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeMs = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var hasBaseline = false
    private var enabled = false

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) {
            hasBaseline = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        hasBaseline = false
        if (enabled && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER || !enabled) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        if (!hasBaseline) {
            lastX = x
            lastY = y
            lastZ = z
            hasBaseline = true
            return
        }
        val gX = x - lastX
        val gY = y - lastY
        val gZ = z - lastZ
        lastX = x
        lastY = y
        lastZ = z
        val delta = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
        if (delta > SHAKE_THRESHOLD) {
            val wall = System.currentTimeMillis()
            if (wall - lastShakeMs > SHAKE_COOLDOWN_MS) {
                lastShakeMs = wall
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val SHAKE_THRESHOLD = 14f
        private const val SHAKE_COOLDOWN_MS = 900L
    }
}
