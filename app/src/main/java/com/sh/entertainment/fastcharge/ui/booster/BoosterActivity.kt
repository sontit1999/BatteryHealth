package com.sh.entertainment.fastcharge.ui.booster

import android.animation.Animator
import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import androidx.core.content.ContextCompat
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.gone
import com.sh.entertainment.fastcharge.common.extension.invisible
import com.sh.entertainment.fastcharge.common.extension.setOnSafeClickListener
import com.sh.entertainment.fastcharge.common.extension.visible
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.databinding.ActivityBoosterBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BoosterActivity : BaseActivityBinding<ActivityBoosterBinding>() {
    private  var didOptimized = false
    override val layoutId = R.layout.activity_booster

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
            if(didOptimized){
                finish()
                return@setOnSafeClickListener
            }
            handleBooster()
            dataBinding.txtMessage.visible()
        }
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
                    dataBinding.btnOptimize.apply {
                        background =
                            ContextCompat.getDrawable(this@BoosterActivity, R.drawable.btn_green)
                        text = getString(R.string.booster_optimized)
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
                dataBinding.txtMessage.text = getString(R.string.ram_free)
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

}