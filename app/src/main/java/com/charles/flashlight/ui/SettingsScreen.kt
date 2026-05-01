package com.charles.flashlight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.flashlight.R
import com.charles.flashlight.data.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: SettingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shake by repository.shakeToToggle.collectAsStateWithLifecycle(initialValue = false)
    val haptics by repository.hapticsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val sound by repository.soundEnabled.collectAsStateWithLifecycle(initialValue = false)
    val autoOff by repository.autoOffMinutes.collectAsStateWithLifecycle(initialValue = 0)
    val strobeHalf by repository.strobeHalfPeriodMs.collectAsStateWithLifecycle(
        initialValue = SettingsRepository.DEFAULT_STROBE_HALF_MS
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_section_behavior),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_shake_toggle)) },
                        leadingContent = {
                            Icon(Icons.Outlined.PhoneAndroid, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = shake,
                                onCheckedChange = { v -> scope.launch { repository.setShakeToToggle(v) } }
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_haptics)) },
                        leadingContent = {
                            Icon(Icons.Outlined.TouchApp, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = haptics,
                                onCheckedChange = { v -> scope.launch { repository.setHapticsEnabled(v) } }
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_sound)) },
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = sound,
                                onCheckedChange = { v -> scope.launch { repository.setSoundEnabled(v) } }
                            )
                        }
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_section_torch),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_strobe_speed),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_strobe_speed_summary, strobeHalf),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    Slider(
                        value = strobeHalf.toFloat(),
                        onValueChange = { v ->
                            scope.launch { repository.setStrobeHalfPeriodMs(v.toInt()) }
                        },
                        valueRange = SettingsRepository.STROBE_HALF_MIN.toFloat()..SettingsRepository.STROBE_HALF_MAX.toFloat(),
                        steps = SettingsRepository.STROBE_HALF_MAX - SettingsRepository.STROBE_HALF_MIN - 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_section_timer),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_auto_off)) },
                        supportingContent = {
                            Text(
                                if (autoOff == 0) {
                                    stringResource(R.string.settings_auto_off_off)
                                } else {
                                    stringResource(R.string.settings_auto_off_minutes, autoOff)
                                }
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Outlined.Timer, contentDescription = null)
                        }
                    )
                    Slider(
                        value = autoOff.toFloat(),
                        onValueChange = { v ->
                            scope.launch { repository.setAutoOffMinutes(v.toInt()) }
                        },
                        valueRange = 0f..30f,
                        steps = 29,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
