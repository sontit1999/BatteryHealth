package com.sh.entertainment.fastcharge.common.util

import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.sh.entertainment.fastcharge.BuildConfig
import com.sh.entertainment.fastcharge.common.extension.adsConfigModel
import com.sh.entertainment.fastcharge.common.extension.shouldShowAds
import com.sh.entertainment.fastcharge.widget.ads.LayoutNativeAd

object AdsManager {

    private const val NATIVE_AD_UNIT_TEST = "ca-app-pub-3940256099942544/2247696110"
    private const val INTERSTITIAL_AD_UNIT_TEST = "ca-app-pub-3940256099942544/1033173712"
    private const val BANNER_AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111"

    private val adRequest by lazy { AdRequest.Builder().build() }
    private var interstitialAdOptimizationResult: InterstitialAd? = null

    fun initMobileAdSdk(ctx: Context) {
        MobileAds.initialize(ctx) {}
    }

    fun showNativeAd(ctx: Context?, layoutNativeAd: LayoutNativeAd, adId: String) {
        if (ctx?.shouldShowAds() == true) {
            // Assign ad id
            val nativeAdId = if (BuildConfig.DEBUG) {
                NATIVE_AD_UNIT_TEST
            } else {
                adId
            }

            // Load and show native ad
            layoutNativeAd.showAd(adRequest, nativeAdId)
        }
    }

    fun destroyNativeAd(layoutNativeAd: LayoutNativeAd) {
        layoutNativeAd.destroyAd()
    }

    fun showBannerAd(
        ctx: Context?,
        container: ViewGroup,
        adId: String,
        adSize: AdSize = AdSize.MEDIUM_RECTANGLE,
        onAdLoaded: (() -> Unit)? = null
    ) {
        if (ctx?.shouldShowAds() == true) {
            // Assign ad id
            val bannerAdId = if (BuildConfig.DEBUG) {
                BANNER_AD_UNIT_TEST
            } else {
                adId
            }

            // Load and show banner ad
            AdView(ctx).apply {
                setAdSize(adSize)
                adUnitId = bannerAdId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        container.addView(this@apply)
                        onAdLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        onAdLoaded?.invoke()
                    }
                }
                loadAd(adRequest)
            }
        }
    }

    fun loadInterstitialOptimizationResult(ctx: Context?) {
        ctx?.run {
            if (shouldShowAds() && adsConfigModel.isInterstitialOptimizeResultEnabled) {
                if (interstitialAdOptimizationResult == null) {
                    interstitialAdOptimizationResult = InterstitialAd(ctx)
                }
                loadInterstitialAd(
                    ctx,
                    interstitialAdOptimizationResult,
                    adsConfigModel.adIdInterstitialOptimizeResult
                )
            }
        }
    }

    fun showInterstitialOptimizationResult(ctx: Context?) {
        ctx?.run {
            if (adsConfigModel.isInterstitialOptimizeResultEnabled) {
                showInterstitialAd(ctx, interstitialAdOptimizationResult)
            }
        }
    }

    private fun loadInterstitialAd(
        ctx: Context?,
        interstitialAd: InterstitialAd?,
        adId: String,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: (() -> Unit)? = null
    ) {
        if (ctx?.shouldShowAds() == true) {
            if (adId.isNotBlank()) {
                // Assign ad id
                val interstitialAdId = if (BuildConfig.DEBUG) {
                    INTERSTITIAL_AD_UNIT_TEST
                } else {
                    adId
                }

                // Load ad
                interstitialAd?.apply {
                    if (adUnitId.isNullOrEmpty()) {
                        adUnitId = interstitialAdId
                    }

                    // Set ad listener
                    if (adListener == null) {
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                onAdLoaded?.invoke()
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError?) {
                                onAdFailedToLoad?.invoke()
                            }
                        }
                    }
                }?.loadAd(adRequest)
            }
        }
    }

    private fun showInterstitialAd(
        ctx: Context?,
        interstitialAd: InterstitialAd?
    ) {
        if (ctx?.shouldShowAds() == true) {
            if (interstitialAd?.isLoaded == true) {
                interstitialAd.show()
            }
        }
    }
}