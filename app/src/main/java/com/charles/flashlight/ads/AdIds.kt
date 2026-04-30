package com.charles.flashlight.ads

import com.charles.flashlight.BuildConfig

object AdIds {
    val interstitial: String = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/1033173712"
    } else {
        "ca-app-pub-8382831211800454/3422794514"
    }
}
