package com.charles.flashlight.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialController(private val context: Context) {
    private var ad: InterstitialAd? = null
    private var loading = false
    private var lastShowElapsedMs: Long = 0L

    fun preload() {
        if (loading || ad != null) return
        loading = true
        InterstitialAd.load(
            context,
            AdIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loading = false
                    ad = interstitialAd
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    ad = null
                }
            }
        )
    }

    /**
     * When the user toggles the torch (on or off): show an interstitial if one is ready and the
     * cooldown has passed, so back-to-back taps do not stack full-screen ads.
     */
    fun maybeShowOnTorchToggle(activity: Activity) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastShowElapsedMs < COOLDOWN_MS) {
            preload()
            return
        }
        val current = ad
        if (current == null) {
            preload()
            return
        }
        lastShowElapsedMs = now
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                preload()
            }
        }
        current.show(activity)
    }

    companion object {
        private const val COOLDOWN_MS = 30_000L
    }
}
