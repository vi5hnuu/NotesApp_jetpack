package com.vi5hnu.notesapp.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Loads and shows an interstitial ad once every [AdConstants.INTERSTITIAL_TRIGGER_INTERVAL]
 * task completions. Pre-loads the next ad immediately after dismissal.
 */
class InterstitialAdManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var completionCount = 0

    init {
        loadAd()
    }

    private fun loadAd() {
        // Guard against overlapping load requests (already loading or already have an ad)
        if (isLoadingAd || interstitialAd != null) return
        isLoadingAd = true
        InterstitialAd.load(
            appContext,
            AdConstants.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingAd = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingAd = false
                }
            }
        )
    }

    /**
     * Call whenever a task is marked complete.
     * Shows the ad at every Nth completion, then pre-loads the next one.
     */
    fun onTaskCompleted(activity: Activity) {
        completionCount++
        if (completionCount % AdConstants.INTERSTITIAL_TRIGGER_INTERVAL != 0) return

        val ad = interstitialAd
        if (ad == null) {
            // No ad ready at the trigger (initial/previous load failed) — retry so it's ready next time
            loadAd()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadAd()
            }
        }
        ad.show(activity)
    }
}
