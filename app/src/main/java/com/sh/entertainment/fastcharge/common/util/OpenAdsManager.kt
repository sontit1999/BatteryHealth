package com.sh.entertainment.fastcharge.common.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.data.model.AdsModel
import com.sh.entertainment.fastcharge.ui.splash.SplashActivity
import org.greenrobot.eventbus.EventBus

@SuppressLint("StaticFieldLeak")
object AppOpenManager : ActivityLifecycleCallbacks,
    LifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallback: AppOpenAdLoadCallback
    private var currentActivity: Activity? = null
    private var isShowingOpenAd = false

    fun start() {
        MyApplication.instance.unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        MyApplication.instance.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        showAdIfAvailable()
        Log.d("HaiHT", "onStart")
    }

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    val isAdAvailable: Boolean
        get() = appOpenAd != null

    private fun showAdIfAvailable() {
        Log.d("HaiHT", appOpenAd.toString())
        if (appOpenAd != null && !isShowingOpenAd && MyApplication.remoteConfigModel.is_open_app
            && (currentActivity != null && currentActivity !is SplashActivity)
            && (System.currentTimeMillis() - MyApplication.timeShowOpenAd) > MyApplication.remoteConfigModel.timeShowAdsOpenApp * 1000) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingOpenAd = false
                        MyApplication.timeShowOpenAd = System.currentTimeMillis()
                        fetchAd()
                        EventBus.getDefault().post(AdsModel(1,false))
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        fetchAd()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingOpenAd = true
                        EventBus.getDefault().post(AdsModel(1,true))
                    }
                }
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            if (currentActivity != null) {
                appOpenAd!!.show(currentActivity!!)
            }
        } else {
            Log.d("HaiHT", "Can not show ad.")
            fetchAd()
        }
    }

    fun fetchAd() {
        if (isAdAvailable) {
            return
        }
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                Log.d("HaiHT", "Load Open Ads Success.")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("HaiHT", "Load Open Ads Error." + loadAdError.message)
            }
        }
        val request = adRequest
        AppOpenAd.load(
            MyApplication.instance, MyApplication.remoteConfigModel.keyOpenAds, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {}

    //----------------------------------------------------------------------------------------------
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

}