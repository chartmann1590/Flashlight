package com.charles.flashlight.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewHelper {
    fun requestReviewIfEligible(activity: Activity, launchCount: Int) {
        if (launchCount != 5 && launchCount != 15) return
        val manager = ReviewManagerFactory.create(activity)
        val flow = manager.requestReviewFlow()
        flow.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }
}
