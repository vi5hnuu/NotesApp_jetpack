package com.vi5hnu.notesapp;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.vi5hnu.notesapp.ads.AppOpenAdManager;

import java.util.concurrent.atomic.AtomicBoolean;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class NoteApplication extends Application {

    private final AtomicBoolean isMobileAdsInitialized = new AtomicBoolean(false);
    private AppOpenAdManager appOpenAdManager;

    public NoteApplication() {
        super();
    }

    /**
     * Initializes the AdMob SDK exactly once. Called only after GDPR/EEA consent has been
     * gathered (see ConsentManager), so ads are never requested before consent.
     */
    public void initializeMobileAdsSdk() {
        if (isMobileAdsInitialized.getAndSet(true)) return;
        MobileAds.initialize(this, initializationStatus -> { });
        appOpenAdManager = new AppOpenAdManager(this);
    }

    public AppOpenAdManager getAppOpenAdManager() {
        return appOpenAdManager;
    }
}
