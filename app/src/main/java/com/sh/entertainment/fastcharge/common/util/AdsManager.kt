package com.sh.entertainment.fastcharge.common.util

import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.sh.entertainment.fastcharge.BuildConfig
import com.sh.entertainment.fastcharge.common.extension.adsConfigModel
import com.sh.entertainment.fastcharge.common.extension.shouldShowAds
import com.sh.entertainment.fastcharge.widget.ads.LayoutNativeAd

object AdsManager {

    val OPEN_AD_KEY: String
    val REWARDED_AD_KEY: String
    val NATIVE_AD_KEY: String
    val BANNER_AD_KEY: String
    var INTERSTITIAL_AD_KEY: String

    private val adRequest by lazy { AdRequest.Builder().build() }
    private var interstitialAdOptimizationResult: InterstitialAd? = null

    init {
        if(BuildConfig.DEBUG){
            NATIVE_AD_KEY = "ca-app-pub-3940256099942544/1044960115"
            BANNER_AD_KEY = "ca-app-pub-3940256099942544/9214589741"
            INTERSTITIAL_AD_KEY = "ca-app-pub-3940256099942544/8691691433"
            REWARDED_AD_KEY = "ca-app-pub-3940256099942544/5224354917"
            OPEN_AD_KEY = "ca-app-pub-3940256099942544/3419835294"
        } else {
            NATIVE_AD_KEY = "ca-app-pub-7254202909466206/8427297816"
            BANNER_AD_KEY = "ca-app-pub-7254202909466206/8455503388"
            INTERSTITIAL_AD_KEY = "ca-app-pub-7254202909466206/1506951650"
            REWARDED_AD_KEY = "ca-app-pub-7254202909466206/1698523341"
            OPEN_AD_KEY = "ca-app-pub-7254202909466206/9577013365"
        }
    }

    fun initMobileAdSdk(ctx: Context) {
        MobileAds.initialize(ctx) {}
    }

    fun showNativeAd(ctx: Context?, layoutNativeAd: LayoutNativeAd, adId: String) {
       /* if (ctx?.shouldShowAds() == true) {
            // Assign ad id
            val nativeAdId = if (BuildConfig.DEBUG) {
                NATIVE_AD_KEY
            } else {
                adId
            }

            // Load and show native ad
            layoutNativeAd.showAd(adRequest, nativeAdId)
        }*/
        layoutNativeAd.showAd(adRequest, adId)
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
                BANNER_AD_KEY
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
                    INTERSTITIAL_AD_KEY
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