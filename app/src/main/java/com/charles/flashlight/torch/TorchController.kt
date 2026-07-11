package com.charles.flashlight.torch

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TorchController {
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    fun initialize(context: Context) {
        if (cameraManager != null) return
        val manager = context.applicationContext.getSystemService(CameraManager::class.java) ?: return
        cameraManager = manager
        cameraId = findFlashCameraId(manager)
    }

    fun hasTorch(context: Context): Boolean {
        initialize(context)
        return cameraId != null
    }

    fun toggleSteady(context: Context): Boolean {
        initialize(context)
        return setSteady(context, !_isActive.value)
    }

    fun setSteady(context: Context, on: Boolean): Boolean {
        val changed = setLight(context, on)
        if (changed || !on) {
            _isActive.value = on
        }
        return changed
    }

    fun setActive(active: Boolean) {
        _isActive.value = active
    }

    fun setLight(context: Context, on: Boolean): Boolean {
        initialize(context)
        val manager = cameraManager ?: return false
        val id = cameraId ?: return false
        return try {
            manager.setTorchMode(id, on)
            true
        } catch (_: CameraAccessException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun findFlashCameraId(manager: CameraManager): String? {
        val candidates = runCatching { manager.cameraIdList.toList() }.getOrNull().orEmpty()
        return candidates.firstOrNull { id ->
            val characteristics = runCatching { manager.getCameraCharacteristics(id) }.getOrNull()
                ?: return@firstOrNull false
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val isBack = characteristics.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_BACK
            hasFlash && isBack
        } ?: candidates.firstOrNull { id ->
            val characteristics = runCatching { manager.getCameraCharacteristics(id) }.getOrNull()
                ?: return@firstOrNull false
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }
}
