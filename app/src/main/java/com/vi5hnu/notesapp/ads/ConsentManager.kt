package com.vi5hnu.notesapp.ads

import android.app.Activity
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Gathers GDPR/EEA consent via Google's User Messaging Platform (UMP) before any ads
 * are requested. [onConsentResolved] is invoked exactly once when the app may proceed to
 * request ads — whether consent was granted, not required (outside the EEA), or the update
 * failed (in which case the SDK serves non-personalised ads).
 */
class ConsentManager(activity: Activity) {

    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

    fun gatherConsent(activity: Activity, onConsentResolved: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated — show the form if the user must make a choice.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onConsentResolved()
                }
            },
            {
                // Update failed — proceed; ads serve as non-personalised.
                onConsentResolved()
            }
        )
    }
}
