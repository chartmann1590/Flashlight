package com.charles.flashlight

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.charles.flashlight.ads.InterstitialController

class MainActivity : ComponentActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var interstitial: InterstitialController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = runCatching { cameraManager.cameraIdList.firstOrNull() }.getOrNull()
        interstitial = InterstitialController(this).also {
            if (BuildConfig.ADS_ENABLED) it.preload()
        }

        setContent {
            MaterialTheme {
                var isOn by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("e2e_home"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.testTag("e2e_torch_toggle"),
                        onClick = {
                            val next = !isOn
                            toggleTorch(next)
                            isOn = next
                            if (next && BuildConfig.ADS_ENABLED) {
                                interstitial.showAlwaysOnTorch(this@MainActivity)
                            }
                        }
                    ) {
                        Text(if (isOn) getString(R.string.turn_off) else getString(R.string.turn_on))
                    }
                    Button(
                        modifier = Modifier.testTag("e2e_about_website"),
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://chartmann1590.github.io/Flashlight/")
                            )
                            startActivity(intent)
                        }
                    ) {
                        Text(getString(R.string.about_website))
                    }
                    Button(
                        modifier = Modifier.testTag("e2e_buy_me_a_coffee"),
                        onClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getString(R.string.buymeacoffee_url))
                                )
                            )
                        }
                    ) {
                        Text(getString(R.string.buy_me_a_coffee))
                    }
                    Button(
                        modifier = Modifier.testTag("e2e_privacy_policy"),
                        onClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getString(R.string.privacy_policy_url))
                                )
                            )
                        }
                    ) {
                        Text(getString(R.string.privacy_policy))
                    }
                }
            }
        }
    }

    private fun toggleTorch(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, enabled)
        } catch (_: CameraAccessException) {
        }
    }
}
