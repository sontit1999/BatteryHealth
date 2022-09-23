package com.sh.entertainment.fastcharge.ui.splash

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.gson.Gson
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.appSettingsModel
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.extension.openActivity
import com.sh.entertainment.fastcharge.common.extension.visible
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.data.model.RemoteConfig
import com.sh.entertainment.fastcharge.ui.base.BaseActivity
import com.sh.entertainment.fastcharge.ui.intro.IntroSliderActivity
import com.sh.entertainment.fastcharge.ui.main.MainActivity

class SplashActivity : BaseActivity<SplashView, SplashPresenterImp>(), SplashView {

    private val progressBar by lazy { findViewById<ContentLoadingProgressBar>(R.id.progress) }
    private val mImgAppName by lazy { findViewById<TextView>(R.id.img_app_name) }
    private val txtLoading by lazy { findViewById<TextView>(R.id.txt_loading) }
    private val mHandler = Handler()

    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallback: AppOpenAd.AppOpenAdLoadCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.checkVipStatus()
        //presenter.getRemoteConfiguration(self)
        loadAnimationImage()
        progressBar.max = 4000
//        presenter.startProgressAnimator(self, progressBar)
    }

    override fun onDestroy() {
        presenter.removeProgressAnimator()
        super.onDestroy()
    }

    override fun initView(): SplashView {
        return this
    }

    override fun initPresenter(): SplashPresenterImp {
        return SplashPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_splash
    }

    override fun initWidgets() {
        hideToolbarBase()
        hideNavigationBar()
        //loadAnimationImage();
    }

    override fun openHomePage() {
        showOpenAds()
    }

    private fun loadAnimationImage() {
        getDataRemoteConfig()
        loadOpenAds()

        val animZoom = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_zoom_in)
        mImgAppName.startAnimation(animZoom)
        animZoom.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val aniSlide = AnimationUtils.loadAnimation(
                    applicationContext, R.anim.anim_slide_up
                )
                mImgAppName.startAnimation(aniSlide)
                aniSlide.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        //anim_slide_up.xmlmImgAppName.setImageBitmap(1.0f)
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        progressBar.visible()
                        txtLoading.visible()
                        mHandler.postDelayed(updateTimerThread, 0)
                        //presenter.startProgressAnimator(self, progressBar)
                    }

                    /*override fun onAnimationEnd(animation: Animation) {
						mAdManager = AdManager.getInstance(SplashActivity.this);
                        if (i == 1) txt_note_ad.setVisibility(View.VISIBLE)
                        mProgress.setVisibility(View.VISIBLE)
                        mHandler.post(mRunnable)
                    }*/

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            val value: Int = progressBar.progress
            progressBar.progress = value + 10
            mHandler.postDelayed(this, 10)
            if (progressBar.progress >= progressBar.max) {
                mHandler.removeCallbacks(this)
                openHomePage()
            }
        }
    }

    private fun getDataRemoteConfig() {
        MyApplication.remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MyApplication.remoteConfig.fetchAndActivate()
                    val dataString = MyApplication.remoteConfig.getString("remote_config")
                    Log.d("HaiHT",dataString)
                    MyApplication.remoteConfigModel =
                        Gson().fromJson(dataString, RemoteConfig::class.java)

                    Log.d("HaiHT",MyApplication.remoteConfigModel.toString())
                } else {
                    MyApplication.remoteConfigModel = RemoteConfig()
                    Log.d("HaiHT","null")
                }
            }
    }

    override fun openIntroPage() {
        openActivity(IntroSliderActivity::class.java)

        // Update settings
        appSettingsModel.apply {
            isFirstTimeAppOpened = false
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    override fun close() {
        finish()
    }

    private fun showOpenAds() {
        Log.d("HaiHT",MyApplication.remoteConfigModel.is_open_app.toString())
        if (appOpenAd != null && MyApplication.remoteConfigModel.is_open_app
            && (System.currentTimeMillis() - MyApplication.timeShowOpenAd) > MyApplication.remoteConfigModel.timeShowAdsOpenApp * 1000) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        MyApplication.timeShowOpenAd = System.currentTimeMillis()
                        openActivity(MainActivity::class.java)
                        finish()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        openActivity(MainActivity::class.java)
                        finish()
                    }

                    override fun onAdShowedFullScreenContent() {
                    }
                }
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd!!.show(this)
        } else {
            openActivity(MainActivity::class.java)
        }
    }

    private fun loadOpenAds() {
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("HaiHT", "Load Open Ads Error.")
            }
        }
        val request = adRequest
        AppOpenAd.load(
            MyApplication.instance, MyApplication.KEY_OPEN_ADS, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()


//    private fun showInter() {
//        if (MyApplication.interstitialAd == null || !MyApplication.remoteConfigModel.isEnableAds) {
//            openActivity(MainActivity::class.java)
//            return
//        }
//        MyApplication.interstitialAd!!.fullScreenContentCallback = object :
//            FullScreenContentCallback() {
//            override fun onAdDismissedFullScreenContent() {
//                openActivity(MainActivity::class.java)
//            }
//
//            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                MyApplication.interstitialAd = null
//            }
//
//            override fun onAdImpression() {}
//            override fun onAdShowedFullScreenContent() {
//            }
//        }
//        MyApplication.interstitialAd!!.show(this)
//    }
//
//    private fun handleLoadInter() {
//        val adRequest = AdRequest.Builder().build()
//        InterstitialAd.load(this, MyApplication.KEY_INTEL, adRequest,
//            object : InterstitialAdLoadCallback() {
//                override fun onAdLoaded(ad: InterstitialAd) {
//                    MyApplication.interstitialAd = ad
//                }
//
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    MyApplication.interstitialAd = null
//                }
//            })
//    }

}
