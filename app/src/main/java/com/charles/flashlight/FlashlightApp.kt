package com.charles.flashlight

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class FlashlightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        MobileAds.initialize(this)
    }
}
