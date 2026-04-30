package com.charles.flashlight.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charles.flashlight.R

@Composable
fun HomeScreen(
    isTorchOn: Boolean,
    onTorchToggle: () -> Unit,
    onAboutWebsite: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onBuyCoffee: () -> Unit
) {
    val glow by animateFloatAsState(
        targetValue = if (isTorchOn) 1f else 0.45f,
        animationSpec = tween(durationMillis = 320),
        label = "torchGlow"
    )

    Box(
        modifier = Modifier
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 320.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    onClick = onTorchToggle,
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
                    color = if (isTorchOn) {
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
                            targetValue = if (isTorchOn) 1.08f else 1f,
                            animationSpec = tween(280),
                            label = "iconPulse"
                        )
                        Icon(
                            imageVector = if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = stringResource(
                                if (isTorchOn) R.string.turn_off else R.string.turn_on
                            ),
                            modifier = Modifier
                                .size(96.dp)
                                .scale(scale),
                            tint = if (isTorchOn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        if (isTorchOn) R.string.torch_on_hint else R.string.torch_off_hint
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onAboutWebsite,
                        modifier = Modifier.testTag("e2e_about_website")
                    ) {
                        Text(
                            text = stringResource(R.string.about_website_short),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TextButton(
                        onClick = onPrivacyPolicy,
                        modifier = Modifier.testTag("e2e_privacy_policy")
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_short),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onBuyCoffee,
                        modifier = Modifier.testTag("e2e_buy_me_a_coffee")
                    ) {
                        Text(
                            text = stringResource(R.string.buy_me_a_coffee),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
