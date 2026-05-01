package com.charles.flashlight.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.charles.flashlight.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun HomeNativeAdSlot(
    adUnitId: String,
    adsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!adsEnabled) return
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    DisposableEffect(adUnitId) {
        loadFailed = false
        nativeAd?.destroy()
        nativeAd = null
        var cancelled = false
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                if (!cancelled) {
                    nativeAd = ad
                } else {
                    ad.destroy()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (!cancelled) {
                        loadFailed = true
                    }
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        onDispose {
            cancelled = true
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    if (loadFailed || nativeAd == null) return

    val ad = nativeAd ?: return
    key(ad) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            factory = { ctx ->
                LayoutInflater.from(ctx).inflate(R.layout.native_ad_layout, null) as NativeAdView
            },
            update = { adView ->
                bindNativeAd(adView, ad)
            },
            onRelease = { }
        )
    }
}

private fun bindNativeAd(adView: NativeAdView, nativeAd: NativeAd) {
    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
    val headline = adView.findViewById<TextView>(R.id.ad_headline)
    val body = adView.findViewById<TextView>(R.id.ad_body)
    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)
    val icon = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val advertiser = adView.findViewById<TextView>(R.id.ad_advertiser)

    adView.mediaView = mediaView
    adView.headlineView = headline
    adView.bodyView = body
    adView.callToActionView = cta
    adView.iconView = icon
    adView.advertiserView = advertiser

    headline.text = nativeAd.headline
    body.text = nativeAd.body
    val ctaText = nativeAd.callToAction
    if (!ctaText.isNullOrBlank()) {
        cta.text = ctaText
        cta.visibility = View.VISIBLE
    } else {
        cta.visibility = View.GONE
    }

    val adv = nativeAd.advertiser
    if (!adv.isNullOrBlank()) {
        advertiser.text = adv
        advertiser.visibility = View.VISIBLE
    } else {
        advertiser.visibility = View.GONE
    }

    val iconDrawable = nativeAd.icon?.drawable
    if (iconDrawable != null) {
        icon.setImageDrawable(iconDrawable)
        icon.visibility = View.VISIBLE
    } else {
        icon.visibility = View.GONE
    }

    val media = nativeAd.mediaContent
    if (media != null) {
        mediaView.setMediaContent(media)
        mediaView.visibility = View.VISIBLE
    } else {
        mediaView.visibility = View.GONE
    }

    adView.setNativeAd(nativeAd)
}

@Composable
fun HomeBannerAdSlot(
    adUnitId: String,
    adsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!adsEnabled || adUnitId.isBlank()) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            val adWidth =
                (ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density).toInt()
            AdView(ctx).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { adView: AdView ->
            adView.destroy()
        }
    )
}
