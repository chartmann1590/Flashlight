package com.charles.flashlight

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.charles.flashlight.ads.InterstitialController
import com.charles.flashlight.ui.HomeScreen
import com.charles.flashlight.ui.theme.FlashlightTheme

class MainActivity : ComponentActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var interstitial: InterstitialController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = runCatching { cameraManager.cameraIdList.firstOrNull() }.getOrNull()
        interstitial = InterstitialController(this).also {
            if (BuildConfig.ADS_ENABLED) it.preload()
        }

        setContent {
            FlashlightTheme {
                var isOn by remember { mutableStateOf(false) }
                HomeScreen(
                    isTorchOn = isOn,
                    onTorchToggle = {
                        val next = !isOn
                        toggleTorch(next)
                        isOn = next
                        if (next && BuildConfig.ADS_ENABLED) {
                            interstitial.showAlwaysOnTorch(this@MainActivity)
                        }
                    },
                    onAboutWebsite = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://chartmann1590.github.io/Flashlight/")
                            )
                        )
                    },
                    onPrivacyPolicy = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.privacy_policy_url))
                            )
                        )
                    },
                    onBuyCoffee = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.buymeacoffee_url))
                            )
                        )
                    }
                )
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
