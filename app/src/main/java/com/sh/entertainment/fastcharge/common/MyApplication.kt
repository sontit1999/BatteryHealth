package com.sh.entertainment.fastcharge.common

import android.app.NotificationManager
import android.util.DisplayMetrics
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.util.*
import com.sh.entertainment.fastcharge.data.model.AdsConfigModel
import com.sh.entertainment.fastcharge.data.model.RemoteConfig
import com.sh.entertainment.fastcharge.ui.base.AppConfig
import java.util.*


class MyApplication : MultiDexApplication() {

    val sharedPref by lazy { SharedPreferencesUtil.customPrefs(ctx) }
    val appSettingsModel by lazy { CommonUtil.getAppSettingsModel(ctx) }
    val adsConfigModel by lazy { AdsConfigModel() }

    var displayMetrics: DisplayMetrics? = null


    companion object {
        lateinit var instance: MyApplication
            private set

        var didOptimized = false

        var countOptimize = 0
        var interstitialAd: InterstitialAd? = null
        var nativeAdExit: UnifiedNativeAd? = null
        var remoteConfigModel = RemoteConfig()
        var brightnessValue = 0

        var problems = 0

        var timeShowIntel = 0L
        var timeShowOpenAd = 0L

        var showRateDialog = MutableLiveData<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppConfig.setUp(this)
        // Init mobile ads SDK
        AdsManager.initMobileAdSdk(this)
        AppOpenManager.start()
        val manager = this.applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        NotificationCenter.createNotificationChannel(manager,NotificationCenter.CHANNEL_ID)
        val random = Random()
        problems = random.nextInt(8)+1
    }

    fun loadNativeExit() {
        val adRequest = AdRequest.Builder()
            .build()
        val adLoader = AdLoader.Builder(ctx, remoteConfigModel.keyNativeExit)
            .forUnifiedNativeAd { nativeAd ->
                nativeAdExit = nativeAd
            }
            .build()
        adLoader.loadAd(adRequest)
    }

}