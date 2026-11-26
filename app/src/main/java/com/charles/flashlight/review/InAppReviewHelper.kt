package com.charles.flashlight.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewHelper {

    /** Completed torch sessions at which a review prompt may be shown. */
    private val SESSION_MILESTONES = setOf(3, 10)

    /**
     * Prompt only after a completed torch session (torch off, message sent) —
     * never at launch and never while the torch is on. Uses the official
     * Play In-App Review API, which applies its own quotas.
     */
    fun requestReviewIfEligible(activity: Activity, completedSessions: Int, torchIsActive: Boolean) {
        if (torchIsActive) return
        if (completedSessions !in SESSION_MILESTONES) return
        val manager = ReviewManagerFactory.create(activity)
        val flow = manager.requestReviewFlow()
        flow.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }
}
