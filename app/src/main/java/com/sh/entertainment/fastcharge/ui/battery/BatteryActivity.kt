package com.sh.entertainment.fastcharge.ui.battery

import android.animation.Animator
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.core.content.ContextCompat
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
        dataBinding.doneAnimation.gone()
        dataBinding.txtMessage.invisible()

        AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }

    override fun initializeData() {
    }

    override fun onClick() {
        dataBinding.btnOptimize.setOnSafeClickListener {
            if (didOptimized) {
                finish()
                return@setOnSafeClickListener
            }
            handleSaverBattery()
            dataBinding.txtMessage.visible()
        }
    }

    private fun handleSaverBattery() {
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
                    saverBattery()
                }

                override fun onAnimationEnd(p0: Animator?) {
                    didOptimized = true
                    dataBinding.lottieAnimation.gone()
                    dataBinding.btnOptimize.apply {
                        background =
                            ContextCompat.getDrawable(this@BatteryActivity, R.drawable.btn_green)
                        text = getString(R.string.battery_optimized)
                    }
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

        dataBinding.doneAnimation.playAnimation()
        dataBinding.doneAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                dataBinding.txtMessage.text = getString(R.string.battery_free)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
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