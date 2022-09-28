package com.sh.entertainment.fastcharge.common

import android.util.DisplayMetrics
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.common.util.AppOpenManager
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.SharedPreferencesUtil
import com.sh.entertainment.fastcharge.data.model.AdsConfigModel
import com.sh.entertainment.fastcharge.data.model.RemoteConfig
import com.sh.entertainment.fastcharge.ui.base.AppConfig


class MyApplication : MultiDexApplication() {

    val sharedPref by lazy { SharedPreferencesUtil.customPrefs(ctx) }
    val appSettingsModel by lazy { CommonUtil.getAppSettingsModel(ctx) }
    val adsConfigModel by lazy { AdsConfigModel() }

    var displayMetrics: DisplayMetrics? = null


    companion object {
        lateinit var instance: MyApplication
            private set
        var countOptimize = 0
        var interstitialAd: InterstitialAd? = null
        var nativeAdExit: UnifiedNativeAd? = null
        lateinit var remoteConfig: FirebaseRemoteConfig
        var remoteConfigModel = RemoteConfig()
        var brightnessValue = 0

        var timeShowIntel = 0L
        var timeShowOpenAd = 0L

        var KEY_INTEL = "ca-app-pub-2238530878125342/7321071062"
        var KEY_OPEN_ADS = "ca-app-pub-2238530878125342/6361721926"
        var KEY_NATIVE = "ca-app-pub-2238530878125342/6302535268"
        var KEY_NATIVE_EXIT = "ca-app-pub-2238530878125342/6302535268"

        var showRateDialog = MutableLiveData<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppConfig.setUp(this)
        // Init mobile ads SDK
        AdsManager.initMobileAdSdk(this)
        setupRemoteConfig()
        AppOpenManager.start()
    }

    fun loadNativeExit() {
        val adRequest = AdRequest.Builder()
            .build()
        val adLoader = AdLoader.Builder(ctx, KEY_NATIVE_EXIT)
            .forUnifiedNativeAd { nativeAd ->
                nativeAdExit = nativeAd
            }
            .build()
        adLoader.loadAd(adRequest)
    }

    private fun setupRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setFetchTimeoutInSeconds(36000).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

}