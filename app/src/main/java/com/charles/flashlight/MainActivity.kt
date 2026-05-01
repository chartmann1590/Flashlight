package com.charles.flashlight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.charles.flashlight.ads.AdIds
import com.charles.flashlight.ads.ConsentManager
import com.charles.flashlight.ads.InterstitialController
import com.charles.flashlight.data.SettingsRepository
import com.charles.flashlight.navigation.FlashlightNavHost
import com.charles.flashlight.review.InAppReviewHelper
import com.charles.flashlight.torch.TorchViewModel
import com.charles.flashlight.ui.ScreenLightActivity
import com.charles.flashlight.ui.theme.FlashlightTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {

    private val torchViewModel: TorchViewModel by viewModels()
    private lateinit var interstitial: InterstitialController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        interstitial = InterstitialController(this)

        setContent {
            FlashlightTheme {
                var monetizationReady by remember { mutableStateOf(!BuildConfig.ADS_ENABLED) }
                val navController = rememberNavController()
                val settingsRepo = remember { SettingsRepository(applicationContext) }

                LaunchedEffect(Unit) {
                    if (BuildConfig.ADS_ENABLED) {
                        suspendCoroutine { cont ->
                            ConsentManager.gatherConsent(this@MainActivity) {
                                interstitial.preload()
                                monetizationReady = true
                                cont.resume(Unit)
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val count = settingsRepo.incrementLaunchCount()
                    InAppReviewHelper.requestReviewIfEligible(this@MainActivity, count)
                }

                LaunchedEffect(Unit) {
                    if (intent?.getBooleanExtra(EXTRA_FROM_WIDGET_TOGGLE, false) == true) {
                        intent.removeExtra(EXTRA_FROM_WIDGET_TOGGLE)
                        torchViewModel.toggle()
                    }
                }

                FlashlightNavHost(
                    navController = navController,
                    torchViewModel = torchViewModel,
                    settingsRepository = settingsRepo,
                    monetizationActive = BuildConfig.ADS_ENABLED && monetizationReady,
                    nativeAdUnitId = AdIds.nativeAdvanced,
                    bannerAdUnitId = AdIds.adaptiveBannerId(this@MainActivity),
                    onTorchToggleForAds = {
                        if (BuildConfig.ADS_ENABLED) {
                            interstitial.maybeShowOnTorchOn(this@MainActivity)
                        }
                    },
                    onOpenWebsite = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://chartmann1590.github.io/Flashlight/")
                            )
                        )
                    },
                    onOpenPrivacy = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.privacy_policy_url))
                            )
                        )
                    },
                    onOpenCoffee = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.buymeacoffee_url))
                            )
                        )
                    },
                    onOpenScreenLight = {
                        startActivity(Intent(this@MainActivity, ScreenLightActivity::class.java))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FROM_WIDGET_TOGGLE, false)) {
            intent.removeExtra(EXTRA_FROM_WIDGET_TOGGLE)
            torchViewModel.toggle()
        }
    }

    companion object {
        const val EXTRA_FROM_WIDGET_TOGGLE = "from_widget_toggle"
    }
}
