package com.vi5hnu.notesapp.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Embeds an AdMob BANNER ad (320×50dp) via [AndroidView].
 *
 * - Height is pre-reserved so the layout doesn't shift when the ad loads.
 * - The [AdView] is remembered across recompositions and destroyed when the composable
 *   leaves composition, preventing the leak of holding a stale ad view.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdConstants.BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    // Release the native AdView when this composable is disposed.
    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        factory = { adView },
        // 50.dp matches AdSize.BANNER height — pre-reserve space to prevent layout shift
        modifier = modifier.fillMaxWidth().height(50.dp)
    )
}
