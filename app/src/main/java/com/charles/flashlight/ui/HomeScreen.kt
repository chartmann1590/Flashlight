package com.charles.flashlight.ui

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.flashlight.R
import com.charles.flashlight.ads.HomeBannerAdSlot
import com.charles.flashlight.ads.HomeNativeAdSlot
import com.charles.flashlight.data.BatteryReader
import com.charles.flashlight.data.SettingsRepository
import com.charles.flashlight.review.InAppReviewHelper
import com.charles.flashlight.sensor.ShakeDetector
import com.charles.flashlight.torch.MorseCode
import com.charles.flashlight.torch.TorchMode
import com.charles.flashlight.torch.TorchViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    torchViewModel: TorchViewModel,
    settingsRepository: SettingsRepository,
    monetizationActive: Boolean,
    nativeAdUnitId: String,
    bannerAdUnitId: String,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenCoffee: () -> Unit,
    onOpenScreenLight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode by torchViewModel.mode.collectAsStateWithLifecycle()
    val isActive by torchViewModel.isActive.collectAsStateWithLifecycle()
    val shakeEnabled by settingsRepository.shakeToToggle.collectAsStateWithLifecycle(initialValue = false)
    val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val soundEnabled by settingsRepository.soundEnabled.collectAsStateWithLifecycle(initialValue = false)
    val autoOffMinutes by settingsRepository.autoOffMinutes.collectAsStateWithLifecycle(initialValue = 0)
    val strobeHalfPeriod by settingsRepository.strobeHalfPeriodMs.collectAsStateWithLifecycle(
        initialValue = SettingsRepository.DEFAULT_STROBE_HALF_MS
    )
    val strobeWarningAck by settingsRepository.strobeWarningAcknowledged.collectAsStateWithLifecycle(
        initialValue = false
    )
    val sessionCount by torchViewModel.successfulSessionCount.collectAsStateWithLifecycle(initialValue = 0)
    val morseText by torchViewModel.morseText.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var remainingAutoOffMs by remember { mutableLongStateOf(0L) }
    var showStrobeWarning by remember { mutableStateOf(false) }
    var batteryInfo by remember { mutableStateOf(BatteryReader.read(context)) }

    // One-shot battery read when the screen appears and when torch state changes.
    LaunchedEffect(isActive) {
        batteryInfo = BatteryReader.read(context)
    }

    // Review prompt only after a completed session, never at launch or while on.
    LaunchedEffect(sessionCount, isActive) {
        val activity = context as? Activity ?: return@LaunchedEffect
        InAppReviewHelper.requestReviewIfEligible(activity, sessionCount, isActive)
    }

    fun performToggle() {
        torchViewModel.toggle()
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        if (soundEnabled) {
            runCatching {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 30)
                    .startTone(ToneGenerator.TONE_PROP_BEEP, 60)
            }
        }
    }

    // Flashing modes (strobe, Morse) are gated by a one-time epilepsy warning.
    fun requestToggle() {
        val needsWarning = !isActive && !strobeWarningAck &&
            (mode == TorchMode.STROBE || mode == TorchMode.MORSE)
        if (needsWarning) {
            showStrobeWarning = true
        } else {
            performToggle()
        }
    }

    DisposableEffect(shakeEnabled, lifecycleOwner) {
        val detector = ShakeDetector(context) { requestToggle() }
        detector.setEnabled(shakeEnabled)
        lifecycleOwner.lifecycle.addObserver(detector)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(detector)
        }
    }

    LaunchedEffect(isActive, autoOffMinutes) {
        if (!isActive || autoOffMinutes <= 0) {
            remainingAutoOffMs = 0L
            return@LaunchedEffect
        }
        val start = SystemClock.elapsedRealtime()
        val total = autoOffMinutes * 60_000L
        coroutineScope {
            launch {
                delay(total)
                torchViewModel.forceOff()
            }
            launch {
                while (true) {
                    val elapsed = SystemClock.elapsedRealtime() - start
                    remainingAutoOffMs = (total - elapsed).coerceAtLeast(0L)
                    if (remainingAutoOffMs <= 0L) break
                    delay(500)
                }
            }
        }
    }

    val glow by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.45f,
        animationSpec = tween(durationMillis = 320),
        label = "torchGlow"
    )

    val modeLabels = listOf(
        TorchMode.STEADY to R.string.mode_steady,
        TorchMode.STROBE to R.string.mode_strobe,
        TorchMode.SOS to R.string.mode_sos,
        TorchMode.MORSE to R.string.mode_morse
    )

    if (showStrobeWarning) {
        AlertDialog(
            onDismissRequest = { showStrobeWarning = false },
            title = { Text(stringResource(R.string.strobe_warning_title)) },
            text = { Text(stringResource(R.string.strobe_warning_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showStrobeWarning = false
                    scope.launch { settingsRepository.setStrobeWarningAcknowledged(true) }
                    performToggle()
                }) {
                    Text(stringResource(R.string.strobe_warning_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStrobeWarning = false }) {
                    Text(stringResource(R.string.strobe_warning_cancel))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("e2e_home")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161018),
                        MaterialTheme.colorScheme.background,
                        Color(0xFF050508)
                    )
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu_more)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateAbout()
                                },
                                modifier = Modifier.testTag("e2e_menu_about")
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_website)) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenWebsite()
                                },
                                modifier = Modifier.testTag("e2e_about_website")
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.privacy_policy)) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenPrivacy()
                                },
                                modifier = Modifier.testTag("e2e_privacy_policy")
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.buy_me_a_coffee)) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenCoffee()
                                },
                                modifier = Modifier.testTag("e2e_buy_me_a_coffee")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (!monetizationActive) Modifier.navigationBarsPadding()
                        else Modifier
                    )
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_tagline_short),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                    batteryInfo?.let { battery ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(R.string.battery_level, battery.percent),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        if (battery.percent <= BatteryReader.LOW_BATTERY_PERCENT && !battery.isCharging) {
                            Text(
                                text = stringResource(R.string.battery_low_warning),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .widthIn(max = 320.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SingleChoiceSegmentedButtonRow {
                            modeLabels.forEachIndexed { index, (torchMode, labelRes) ->
                                SegmentedButton(
                                    selected = mode == torchMode,
                                    onClick = { torchViewModel.setMode(torchMode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = modeLabels.size
                                    )
                                ) {
                                    Text(stringResource(labelRes))
                                }
                            }
                        }
                    }

                    if (mode == TorchMode.MORSE) {
                        OutlinedTextField(
                            value = morseText,
                            onValueChange = { torchViewModel.setMorseText(it) },
                            label = { Text(stringResource(R.string.morse_input_label)) },
                            placeholder = { Text(stringResource(R.string.morse_input_hint)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("e2e_morse_input")
                        )
                    }

                    if (mode == TorchMode.STROBE) {
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presetLabels = listOf(
                                R.string.strobe_preset_slow,
                                R.string.strobe_preset_medium,
                                R.string.strobe_preset_fast
                            )
                            SettingsRepository.STROBE_PRESETS_MS.forEachIndexed { index, presetMs ->
                                FilterChip(
                                    selected = strobeHalfPeriod == presetMs,
                                    onClick = {
                                        scope.launch {
                                            settingsRepository.setStrobeHalfPeriodMs(presetMs)
                                        }
                                    },
                                    label = { Text(stringResource(presetLabels[index])) }
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = onOpenScreenLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.LightMode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.screen_light_title))
                    }

                    Surface(
                        onClick = { requestToggle() },
                        modifier = Modifier
                            .size(248.dp)
                            .shadow(
                                elevation = (8f + 20f * glow).dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f * glow),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f * glow)
                            )
                            .testTag("e2e_torch_toggle"),
                        shape = CircleShape,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f + 0.35f * glow)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1.08f else 1f,
                                animationSpec = tween(280),
                                label = "iconPulse"
                            )
                            Icon(
                                imageVector = if (isActive) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = stringResource(
                                    if (isActive) R.string.turn_off else R.string.turn_on
                                ),
                                modifier = Modifier
                                    .size(96.dp)
                                    .scale(scale),
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val modeLine = when (mode) {
                        TorchMode.STEADY -> stringResource(R.string.status_mode_steady)
                        TorchMode.STROBE -> stringResource(
                            R.string.status_mode_strobe_ms,
                            strobeHalfPeriod * 2
                        )
                        TorchMode.SOS -> stringResource(R.string.status_mode_sos)
                        TorchMode.MORSE -> stringResource(R.string.status_mode_morse)
                    }
                    Text(
                        text = if (isActive) {
                            buildString {
                                append(modeLine)
                                append("\n")
                                append(stringResource(R.string.torch_on_hint))
                                if (autoOffMinutes > 0 && remainingAutoOffMs > 0L) {
                                    append("\n")
                                    append(
                                        stringResource(
                                            R.string.status_auto_off_remaining,
                                            formatRemaining(remainingAutoOffMs)
                                        )
                                    )
                                }
                            }
                        } else {
                            stringResource(R.string.torch_off_hint)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (monetizationActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HomeNativeAdSlot(
                        adUnitId = nativeAdUnitId,
                        adsEnabled = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    HomeBannerAdSlot(
                        adUnitId = bannerAdUnitId,
                        adsEnabled = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatRemaining(ms: Long): String {
    val m = TimeUnit.MILLISECONDS.toMinutes(ms)
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}
