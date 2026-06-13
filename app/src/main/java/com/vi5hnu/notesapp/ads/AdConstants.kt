package com.vi5hnu.notesapp.ads

import com.vi5hnu.notesapp.BuildConfig

/**
 * Central place for all AdMob unit IDs and configuration.
 *
 * Debug builds always use Google's public test ad units — serving real ads in debug
 * (and accidentally clicking them) can get the AdMob account suspended.
 */
object AdConstants {
    val BANNER_AD_UNIT_ID = if (BuildConfig.DEBUG)
        "ca-app-pub-3940256099942544/6300978111"
    else
        "ca-app-pub-4715945578201106/5098577721"

    val INTERSTITIAL_AD_UNIT_ID = if (BuildConfig.DEBUG)
        "ca-app-pub-3940256099942544/1033173712"
    else
        "ca-app-pub-4715945578201106/3067849755"

    val APP_OPEN_AD_UNIT_ID = if (BuildConfig.DEBUG)
        "ca-app-pub-3940256099942544/9257395921"
    else
        "ca-app-pub-4715945578201106/3959430457"

    /** Show interstitial once every N task completions. */
    const val INTERSTITIAL_TRIGGER_INTERVAL = 5
}
