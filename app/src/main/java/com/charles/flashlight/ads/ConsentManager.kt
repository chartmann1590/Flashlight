package com.charles.flashlight.ads

import android.app.Activity
import com.charles.flashlight.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {
    fun gatherConsent(activity: Activity, onFinished: () -> Unit) {
        val paramsBuilder = ConsentRequestParameters.Builder()
        if (BuildConfig.DEBUG) {
            paramsBuilder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA)
                    .build()
            )
        }
        val params = paramsBuilder.build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    activity.runOnUiThread(onFinished)
                }
            },
            { _ ->
                activity.runOnUiThread(onFinished)
            }
        )
    }
}
