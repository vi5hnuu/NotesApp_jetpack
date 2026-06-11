package com.vi5hnu.notesapp.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Composable that embeds an AdMob BANNER ad (320×50dp) via [AndroidView].
 * Height is pre-reserved so the layout doesn't shift when the ad loads.
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
        // 50.dp matches AdSize.BANNER height — pre-reserve space to prevent layout shift
        modifier = modifier.fillMaxWidth().height(50.dp)
    )
}
