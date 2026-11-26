package com.charles.flashlight.torch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charles.flashlight.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TorchViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _mode = MutableStateFlow(TorchMode.STEADY)
    val mode: StateFlow<TorchMode> = _mode.asStateFlow()

    val isActive: StateFlow<Boolean> = TorchController.isActive

    private val strobeHalfPeriodMs = MutableStateFlow(SettingsRepository.DEFAULT_STROBE_HALF_MS)

    private val _morseText = MutableStateFlow("")
    val morseText: StateFlow<String> = _morseText.asStateFlow()

    /** Completed torch sessions, used to time the in-app review prompt. */
    val successfulSessionCount = settingsRepository.successfulSessionCount

    private var patternJob: Job? = null

    init {
        TorchController.initialize(application)
        viewModelScope.launch {
            var wasActive = TorchController.isActive.value
            TorchController.isActive.collect { active ->
                if (wasActive && !active) {
                    settingsRepository.incrementSuccessfulSessionCount()
                }
                wasActive = active
            }
        }
        viewModelScope.launch {
            settingsRepository.strobeHalfPeriodMs
                .distinctUntilChanged()
                .collect { ms ->
                    strobeHalfPeriodMs.value = ms
                    if (_mode.value == TorchMode.STROBE && TorchController.isActive.value) {
                        restartPattern()
                    }
                }
        }
    }

    fun setMode(mode: TorchMode) {
        _mode.value = mode
        if (TorchController.isActive.value) {
            restartPattern()
        }
    }

    fun setMorseText(text: String) {
        _morseText.value = MorseCode.sanitize(text)
    }

    fun toggle() {
        val next = !TorchController.isActive.value
        patternJob?.cancel()
        patternJob = null
        if (!next) {
            setSteady(false)
            return
        }
        if (!TorchController.hasTorch(getApplication())) return
        when (_mode.value) {
            TorchMode.STEADY -> setSteady(true)
            TorchMode.STROBE -> {
                TorchController.setActive(true)
                startStrobe()
            }
            TorchMode.SOS -> {
                TorchController.setActive(true)
                startSos()
            }
            TorchMode.MORSE -> {
                if (MorseCode.encode(_morseText.value).isEmpty()) return
                TorchController.setActive(true)
                startMorse()
            }
        }
    }

    fun forceOff() {
        patternJob?.cancel()
        patternJob = null
        setSteady(false)
    }

    private fun restartPattern() {
        patternJob?.cancel()
        patternJob = null
        setLight(false)
        if (!TorchController.isActive.value) return
        when (_mode.value) {
            TorchMode.STEADY -> setSteady(true)
            TorchMode.STROBE -> startStrobe()
            TorchMode.SOS -> startSos()
            TorchMode.MORSE -> startMorse()
        }
    }

    private fun startStrobe() {
        patternJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive && TorchController.isActive.value) {
                val half = strobeHalfPeriodMs.value.toLong().coerceAtLeast(1L)
                setLight(true)
                delay(half)
                setLight(false)
                delay(half)
            }
        }
    }

    private fun startSos() {
        patternJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive && TorchController.isActive.value) {
                for (i in 0 until 3) dot()
                delay(LETTER_GAP_MS)
                for (i in 0 until 3) dash()
                delay(LETTER_GAP_MS)
                for (i in 0 until 3) dot()
                delay(WORD_GAP_MS)
            }
        }
    }

    /** Transmit the current Morse message once, then turn the torch off. */
    private fun startMorse() {
        val symbols = MorseCode.encode(_morseText.value)
        if (symbols.isEmpty()) {
            TorchController.setActive(false)
            return
        }
        patternJob = viewModelScope.launch {
            for (symbol in symbols) {
                if (!TorchController.isActive.value) return@launch
                when (symbol) {
                    '.' -> dot()
                    '-' -> dash()
                    ' ' -> delay(LETTER_GAP_MS)
                    '/' -> delay(WORD_GAP_MS)
                }
            }
            // Message complete: end the session (counts toward review timing).
            setLight(false)
            TorchController.setActive(false)
        }
    }

    private suspend fun dot() {
        if (!TorchController.isActive.value) return
        setLight(true)
        delay(DOT_MS)
        setLight(false)
        delay(SYMBOL_GAP_MS)
    }

    private suspend fun dash() {
        if (!TorchController.isActive.value) return
        setLight(true)
        delay(DASH_MS)
        setLight(false)
        delay(SYMBOL_GAP_MS)
    }

    private fun setSteady(on: Boolean) {
        TorchController.setSteady(getApplication(), on)
    }

    private fun setLight(on: Boolean) {
        TorchController.setLight(getApplication(), on)
    }

    override fun onCleared() {
        patternJob?.cancel()
        setSteady(false)
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
