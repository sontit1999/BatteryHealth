package com.sh.entertainment.fastcharge.ui.booster

import android.animation.Animator
import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.gone
import com.sh.entertainment.fastcharge.common.extension.invisible
import com.sh.entertainment.fastcharge.common.extension.visible
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.data.model.AdsModel
import com.sh.entertainment.fastcharge.databinding.ActivityBoosterBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BoosterActivity : BaseActivityBinding<ActivityBoosterBinding>() {
    private  var didOptimized = false
    override val layoutId = R.layout.activity_booster

    override fun initializeView() {
        dataBinding.imgAvatar.gone()
        dataBinding.doneAnimation.gone()
        dataBinding.txtMessage.invisible()
        if(MyApplication.remoteConfigModel.isEnableAds && MyApplication.remoteConfigModel.is_native_result){
            AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)
        }
    }

    override fun initializeData() {
        handleLoadInter()

        handleBooster()
        dataBinding.txtMessage.visible()
    }

    override fun onClick() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdsModelEvent(adsModel: AdsModel) {
        Log.d("HaiHT", "onAdsModelEvent")
        // Do something
        if(adsModel.type == 1) {
            if(adsModel.isShow){
                // hidden native ads
                dataBinding.nativeAdView.gone()
            } else {
                // show native ads
                dataBinding.nativeAdView.visible()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun handleBooster() {
        dataBinding.lottieAnimation.apply {
            repeatCount = 4
            setAnimation(R.raw.scan)
            removeAllAnimatorListeners()
            cancelAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    dataBinding.txtMessage.text = getString(R.string.optimizing)
                    dataBinding.imgAvatar.visible()
                    bindViewAvatar()
                }

                override fun onAnimationEnd(p0: Animator?) {
                    didOptimized =true
                    dataBinding.lottieAnimation.gone()
//                    dataBinding.btnOptimize.apply {
//                        background =
//                            ContextCompat.getDrawable(this@BoosterActivity, R.drawable.btn_green)
//                        text = getString(R.string.booster_optimized)
//                    }
                    handlerStartAminDone()
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            playAnimation()
        }

    }

    private fun handlerStartAminDone() {
        dataBinding.lottieAnimation.gone()
        dataBinding.doneAnimation.visible()
        dataBinding.imgAvatar.gone()

        dataBinding.txtMessage.text = getString(R.string.ram_free)
        dataBinding.doneAnimation.playAnimation()
        dataBinding.doneAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if(MyApplication.remoteConfigModel.isEnableAds && MyApplication.remoteConfigModel.is_inter_result){
                    showInter()
                }else{
                    finish()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
    }

    private fun bindViewAvatar() {
        CoroutineScope(Dispatchers.Main).launch {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            packageManager.getInstalledApplications(0).forEach {
                if (it.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                    delay(200)
                    val avatar = packageManager.getApplicationIcon(it.packageName)
                    dataBinding.imgAvatar.setImageDrawable(avatar)
                    if (it.packageName != packageName) {
                        activityManager.killBackgroundProcesses(it.packageName)
                    }
                }
            }
        }
    }

    private fun showInter() {
        if (MyApplication.interstitialAd == null) {
            finish()
            return
        }
        if ((System.currentTimeMillis() - MyApplication.timeShowIntel) < MyApplication.remoteConfigModel.timeShowInter * 1000) {
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
        InterstitialAd.load(this, MyApplication.remoteConfigModel.keyIntel, adRequest,
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