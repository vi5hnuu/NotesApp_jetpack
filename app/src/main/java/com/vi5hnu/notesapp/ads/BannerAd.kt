package com.vi5hnu.notesapp.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Composable that embeds an AdMob banner ad (320x50) via [AndroidView].
 * The view is stable — AdMob handles refreshing internally.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdConstants.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier
    )
}
