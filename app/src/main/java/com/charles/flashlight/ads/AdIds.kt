package com.charles.flashlight.ads

import android.content.Context
import com.charles.flashlight.BuildConfig
import com.charles.flashlight.R

object AdIds {
    private const val GOOGLE_BANNER_TEST = "ca-app-pub-3940256099942544/9214589741"
    private const val GOOGLE_NATIVE_TEST = "ca-app-pub-3940256099942544/2247696110"

    val interstitial: String =
        if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-8382831211800454/3422794514"
        }

    val nativeAdvanced: String =
        if (BuildConfig.DEBUG) {
            GOOGLE_NATIVE_TEST
        } else {
            "ca-app-pub-8382831211800454/6282727530"
        }

    /** Adaptive banner: always returns a unit id for loading (test in debug; release string or test fallback). */
    fun adaptiveBannerId(context: Context): String =
        if (BuildConfig.DEBUG) {
            GOOGLE_BANNER_TEST
        } else {
            val id = context.getString(R.string.admob_banner_ad_unit_id).trim()
            if (id.isNotEmpty()) id else GOOGLE_BANNER_TEST
        }
}
