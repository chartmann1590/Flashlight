package com.charles.flashlight

import android.app.Application
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class FlashlightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crashlytics only here. MobileAds and Firebase Analytics are NOT
        // initialised in Application.onCreate: they start in MainActivity
        // after UMP consent, and only when canRequestAds() is true, so the
        // first frame (and the torch) is never blocked on ad/analytics SDKs.
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
