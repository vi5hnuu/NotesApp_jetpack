package com.vi5hnu.notesapp.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Manages App Open ads shown on cold start and each time the app returns to foreground.
 *
 * Lifecycle:
 * - Registered as [Application.ActivityLifecycleCallbacks] to track the foreground activity.
 * - Registered as [DefaultLifecycleObserver] on [ProcessLifecycleOwner] to detect app<->background transitions.
 * - Ad is valid for 4 hours; a fresh load is triggered after show/dismiss.
 */
class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var isBackground = false
    private var loadTime: Long = 0
    var currentActivity: Activity? = null
        private set

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadAd()
    }

    private fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        AppOpenAd.load(
            application,
            AdConstants.APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    /** Ad expires 4 hours after loading per AdMob guidelines. */
    private fun isAdAvailable(): Boolean {
        val elapsed = Date().time - loadTime
        return appOpenAd != null && elapsed < 4 * 3_600_000L
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) return
        if (!isAdAvailable()) {
            loadAd()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }
        }
        appOpenAd?.show(activity)
    }

    // ---- ProcessLifecycleOwner callbacks ----

    override fun onStop(owner: LifecycleOwner) {
        // App went to background (only mark if the ad itself didn't cause it)
        if (!isShowingAd) isBackground = true
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isBackground) {
            isBackground = false
            currentActivity?.let { showAdIfAvailable(it) }
        }
    }

    // ---- ActivityLifecycleCallbacks ----

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) currentActivity = null
    }
}
