package com.vi5hnu.notesapp;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.vi5hnu.notesapp.ads.AppOpenAdManager;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class NoteApplication extends Application {

    private AppOpenAdManager appOpenAdManager;

    public NoteApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize AdMob SDK; AppOpenAdManager is created inside the callback
        // to ensure the SDK is ready before loading the first ad.
        MobileAds.initialize(this, initializationStatus ->
                appOpenAdManager = new AppOpenAdManager(this)
        );
    }

    public AppOpenAdManager getAppOpenAdManager() {
        return appOpenAdManager;
    }
}
