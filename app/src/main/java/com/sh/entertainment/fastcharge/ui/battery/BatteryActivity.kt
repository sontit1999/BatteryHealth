package com.sh.entertainment.fastcharge.ui.battery

import android.animation.Animator
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.core.content.ContextCompat
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
import com.sh.entertainment.fastcharge.databinding.ActivityBatteryBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BatteryActivity : BaseActivityBinding<ActivityBatteryBinding>() {

    private var didOptimized = false

    override val layoutId = R.layout.activity_battery

    override fun initializeView() {
        dataBinding.imgAvatar.gone()
        dataBinding.optimize.gone()
        dataBinding.doneAnimation.gone()
        AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }

    override fun initializeData() {
        handleLoadInter()

        dataBinding.scanApp.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                dataBinding.txtMessage.text = getString(R.string.scan_app_battery)
                CoroutineScope(Dispatchers.Main).launch {
                    val list = packageManager.getInstalledApplications(0).filter {
                        packageManager.getLaunchIntentForPackage(it.packageName) != null
                    }
                    dataBinding.txtCountApp.text = list.size.toString()
                    list.forEach {
                        delay(200)
                        dataBinding.txtDescription.text = it.loadLabel(packageManager)
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                dataBinding.scanApp.gone()
                dataBinding.txtDescription.gone()
                dataBinding.optimize.visible()
                handleSaverBattery()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            dataBinding.scanApp.playAnimation()
        }
    }

    override fun onClick() {
//        dataBinding.btnOptimize.setOnSafeClickListener {
//            if (didOptimized) {
//                finish()
//                return@setOnSafeClickListener
//            }
//            handleSaverBattery()
//            dataBinding.txtMessage.visible()
//        }
    }

    private fun handleSaverBattery() {
        dataBinding.optimize.apply {
            repeatCount = 3
            setAnimation(R.raw.scan)
            removeAllAnimatorListeners()
            cancelAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    dataBinding.txtMessage.text = getString(R.string.optimizing)
                    dataBinding.imgAvatar.visible()
                    bindViewAvatar()
                    saverBattery()
                }

                override fun onAnimationEnd(p0: Animator?) {
                    didOptimized = true
                    dataBinding.optimize.gone()
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
        dataBinding.doneAnimation.visible()
        dataBinding.imgAvatar.gone()

        dataBinding.doneAnimation.playAnimation()
        dataBinding.doneAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                dataBinding.txtMessage.text = getString(R.string.battery_free)
                showInter()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
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

    //----------------------------------------------------------------------------------------------
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
                        optimizeParticularApp(it.packageName)
                    }
                }
            }
        }
    }

    private fun saverBattery() {
        killBackgroundApps()
        toggleBluetooth(false)
        toggleWifi(false)
        toggleAutoSync(false)
        toggleScreenRotation(0)
        updateBrightness()
        CoroutineScope(Dispatchers.IO).launch {
            packageManager.getInstalledApplications(0).forEach {
                optimizeParticularApp(it.packageName)
                enableStandbyApp(it.packageName)
            }
        }
    }

    //Turn on Battery Optimization for a particular app
    private fun optimizeParticularApp(packageName: String) {
        val cmd = "dumpsys deviceidle whitelist +$packageName"
        Runtime.getRuntime().exec(cmd)
    }

    //Force the app into App Standby mode by running the following commands
    private fun enableStandbyApp(packageName: String) {
        val cmdDumpsys = "dumpsys battery unplug"
        Runtime.getRuntime().exec(cmdDumpsys)
        val cmdInActive = "am set-inactive $packageName true"
        Runtime.getRuntime().exec(cmdInActive)
    }

    private fun updateBrightness() {
        if (canWriteSettings() && appSettingsModel.isReduceScreenTimeOut) {
            try {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                val brightness = Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
                MyApplication.brightnessValue = brightness
                if (brightness < 40) return
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, 40
                )
                window.changeAppScreenBrightnessValue(40F)

            } catch (e: Settings.SettingNotFoundException) {
                Log.e("Error", "Cannot access system brightness")
                e.printStackTrace()
            }
        }
    }

    private fun Window.changeAppScreenBrightnessValue(brightnessValue: Float) {
        val layoutParams = this.attributes
        layoutParams.screenBrightness = brightnessValue
        this.attributes = layoutParams
    }

    private fun killBackgroundApps() {
        try {
            val packages = ctx.getInstalledApps()
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            activityManager?.run {
                for (packageInfo in packages) {
                    killBackgroundProcesses(packageInfo.packageName)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun toggleBluetooth(enable: Boolean) {
        if (enable) {
            if (!ctx.isBluetoothEnabled) {
                ctx.toggleBluetooth(true)
            }
        } else {
            if (ctx.isBluetoothEnabled) {
                ctx.toggleBluetooth(false)
            }
        }
    }

    private fun toggleWifi(enable: Boolean) {
        if (enable) {
            if (!ctx.isWifiEnabled) {
                ctx.toggleWifi(true)
            }
        } else {
            if (ctx.isWifiEnabled) {
                ctx.toggleWifi(false)
            }
        }
    }

    private fun toggleAutoSync(enable: Boolean) {
        if (ctx.isAutoSyncEnabled) {
            if (!enable) {
                ctx.toggleAutoSync(false)
            }
        } else {
            if (enable) {
                ctx.toggleAutoSync(true)
            }
        }
    }

    private fun toggleScreenRotation(value: Int) {
        if (ctx.canWriteSettings()) {
            if (ctx.isAutoRotationEnabled) {
                if (value == 0) {
                    ctx.toggleAutoRotation(0)
                }
            } else {
                if (value == 1) {
                    ctx.toggleAutoRotation(1)
                }
            }
        }
    }

}