package com.charles.flashlight

import android.app.Application
import com.google.android.gms.ads.MobileAds

class FlashlightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
