package com.charles.flashlight.torch

import android.app.Application
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charles.flashlight.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TorchViewModel(application: Application) : AndroidViewModel(application) {

    private val cameraManager = application.getSystemService(CameraManager::class.java)!!
    private val cameraId: String? = runCatching { cameraManager.cameraIdList.firstOrNull() }.getOrNull()
    private val settingsRepository = SettingsRepository(application)

    private val _mode = MutableStateFlow(TorchMode.STEADY)
    val mode: StateFlow<TorchMode> = _mode.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val strobeHalfPeriodMs = MutableStateFlow(SettingsRepository.DEFAULT_STROBE_HALF_MS)

    private var patternJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.strobeHalfPeriodMs
                .distinctUntilChanged()
                .collect { ms ->
                    strobeHalfPeriodMs.value = ms
                    if (_mode.value == TorchMode.STROBE && _isActive.value) {
                        restartPattern()
                    }
                }
        }
    }

    fun setMode(mode: TorchMode) {
        _mode.value = mode
        if (_isActive.value) {
            restartPattern()
        }
    }

    fun toggle() {
        val next = !_isActive.value
        _isActive.value = next
        patternJob?.cancel()
        patternJob = null
        if (!next) {
            setTorch(false)
            return
        }
        when (_mode.value) {
            TorchMode.STEADY -> setTorch(true)
            TorchMode.STROBE -> startStrobe()
            TorchMode.SOS -> startSos()
        }
    }

    fun forceOff() {
        patternJob?.cancel()
        patternJob = null
        _isActive.value = false
        setTorch(false)
    }

    private fun restartPattern() {
        patternJob?.cancel()
        patternJob = null
        setTorch(false)
        if (!_isActive.value) return
        when (_mode.value) {
            TorchMode.STEADY -> setTorch(true)
            TorchMode.STROBE -> startStrobe()
            TorchMode.SOS -> startSos()
        }
    }

    private fun startStrobe() {
        patternJob = viewModelScope.launch {
            while (isActive && _isActive.value) {
                val half = strobeHalfPeriodMs.value.toLong().coerceAtLeast(1L)
                setTorch(true)
                delay(half)
                setTorch(false)
                delay(half)
            }
        }
    }

    private fun startSos() {
        patternJob = viewModelScope.launch {
            while (isActive && _isActive.value) {
                for (i in 0 until 3) dot()
                delay(LETTER_GAP_MS)
                for (i in 0 until 3) dash()
                delay(LETTER_GAP_MS)
                for (i in 0 until 3) dot()
                delay(WORD_GAP_MS)
            }
        }
    }

    private suspend fun dot() {
        if (!_isActive.value) return
        setTorch(true)
        delay(DOT_MS)
        setTorch(false)
        delay(SYMBOL_GAP_MS)
    }

    private suspend fun dash() {
        if (!_isActive.value) return
        setTorch(true)
        delay(DASH_MS)
        setTorch(false)
        delay(SYMBOL_GAP_MS)
    }

    private fun setTorch(on: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, on)
        } catch (_: CameraAccessException) {
        }
    }

    override fun onCleared() {
        patternJob?.cancel()
        setTorch(false)
        super.onCleared()
    }

    companion object {
        private const val DOT_MS = 200L
        private const val DASH_MS = 600L
        private const val SYMBOL_GAP_MS = 200L
        private const val LETTER_GAP_MS = 400L
        private const val WORD_GAP_MS = 1400L
    }
}
