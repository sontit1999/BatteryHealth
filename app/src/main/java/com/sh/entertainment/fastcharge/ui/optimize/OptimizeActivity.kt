package com.sh.entertainment.fastcharge.ui.optimize

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.databinding.FragmentOptimizeBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding

class OptimizeActivity : BaseActivityBinding<FragmentOptimizeBinding>() {

    private fun bindingAction() {
        dataBinding.animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                finish()
            }

            override fun onAnimationRepeat(animation: Animator) {
                super.onAnimationRepeat(animation)
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
            }

            override fun onAnimationPause(animation: Animator) {
                super.onAnimationPause(animation)
            }

            override fun onAnimationResume(animation: Animator) {
                super.onAnimationResume(animation)
            }
        })
    }

    private fun loadNativeAds() {
        AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }

    override fun initializeView() {
        loadNativeAds()
        bindingAction()
    }

    override fun initializeData() {

    }

    override fun onClick() {

    }

    override val layoutId = R.layout.fragment_optimize
}