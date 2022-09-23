package com.sh.entertainment.fastcharge.ui.boresult

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.ui.base.BaseActivity
import com.sh.entertainment.fastcharge.widget.ads.LayoutNativeAd

class OptimizationResultActivity :
    BaseActivity<OptimizationResultView, OptimizationResultPresenterImp>(), OptimizationResultView {

    companion object {
        const val RC_DRAW_OVERLAY = 256
//        private const val RC_DRAW_OVERLAY = 257
    }

    private val btnOk by lazy { findViewById<TextView>(R.id.btn_ok) }
    private val nativeAd by lazy { findViewById<LayoutNativeAd>(R.id.nativeAdView) }

    private var shouldCheckShowingRateDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        parseIntent(intent)
        super.onCreate(savedInstanceState)
    }

    override fun initView(): OptimizationResultView {
        return this
    }

    override fun initPresenter(): OptimizationResultPresenterImp {
        return OptimizationResultPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_optimization_result
    }

    override fun initWidgets() {
        // Hide base toolbar
        hideToolbarBase()
        handleLoadInter()

        // Show rate dialog or show interstitial ad
        if (shouldCheckShowingRateDialog) {
            appSettingsModel.run {
                if (isShowRateDialog()) {
                    val isShowCheckBox = clickedRateButtonTimes == 1
                    showRateAppDialog(isShowCheckBox)
                } else {
                    AdsManager.showInterstitialOptimizationResult(ctx)
                }
            }
        }
        loadAnimationImage()
        MyApplication.countOptimize++

        val adRequest = AdRequest.Builder()
            .build()
        if (MyApplication.remoteConfigModel.is_native_result) {
            nativeAd.showAd(adRequest, MyApplication.KEY_NATIVE)

        }

        // Listeners
        btnOk.setOnSafeClickListener {
            if (MyApplication.countOptimize %2 ==0 && MyApplication.countOptimize <8) {
                if (!canDrawOverlay()) {
                    showDrawOverlayPermissionDescDialog(onOkListener = {
                        requestDrawOverlayPermission(self, RC_DRAW_OVERLAY)
                    }, onCancelListener = {
                        finish()
                    })
                } else {
                    finish()
                }
            } else if(MyApplication.countOptimize %2 ==0 && MyApplication.countOptimize >=8) {
                finish()
                MyApplication.showRateDialog.postValue(true)
            }else{
                showInter()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DRAW_OVERLAY && canDrawOverlay()) {
            setResult(RESULT_OK)
            finish()
        } else {
            finish()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loadAnimationImage() {
        val animZoom = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_zoom_in)
        animZoom.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val aniSlide = AnimationUtils.loadAnimation(
                    applicationContext, R.anim.anim_slide_up
                )
                aniSlide.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        //anim_slide_up.xmlmImgAppName.setImageBitmap(1.0f)
                    }

                    override fun onAnimationEnd(p0: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun parseIntent(intent: Intent?) {
        shouldCheckShowingRateDialog =
            intent?.extras?.getBoolean(com.sh.entertainment.fastcharge.common.Constants.KEY_CHECK_SHOWING_RATE_DIALOG) ?: false
    }

    private fun showInter() {
        if (MyApplication.interstitialAd == null || !MyApplication.remoteConfigModel.is_inter_back_info) {
            finish()
            return
        }
        if ((System.currentTimeMillis() - MyApplication.timeShowIntel) < MyApplication.remoteConfigModel.timeShowInter*1000) {
            finish()
            return
        }
        MyApplication.interstitialAd!!.fullScreenContentCallback = object :
            FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finish()
                MyApplication.timeShowIntel = System.currentTimeMillis()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                finish()
                MyApplication.interstitialAd = null
            }

            override fun onAdImpression() {}
            override fun onAdShowedFullScreenContent() {
            }
        }
        MyApplication.interstitialAd!!.show(this)
    }

    private fun handleLoadInter() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, MyApplication.KEY_INTEL, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    MyApplication.interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    MyApplication.interstitialAd = null
                }
            })
    }

}
