package com.procharger.fastprocharrging.quickcharge.ui.splash

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.procharger.fastprocharrging.quickcharge.common.MyApplication
import com.procharger.fastprocharrging.quickcharge.common.extension.appSettingsModel
import com.procharger.fastprocharrging.quickcharge.data.interactor.FirebaseRemoteInteractor
import com.procharger.fastprocharrging.quickcharge.data.interactor.IapInteractor
import com.procharger.fastprocharrging.quickcharge.ui.base.BasePresenterImp

private const val DELAY_BEFORE_LOADING_PROGRESS_BAR = 300L // 300 milliseconds
private const val PROGRESS_BAR_DURATION = 1500L // 1.5 seconds

class SplashPresenterImp(private val ctx: Context) : BasePresenterImp<SplashView>(ctx) {

    private val iapInteractor by lazy { IapInteractor(ctx) }
    private val firebaseRemoteInteractor by lazy { FirebaseRemoteInteractor() }
    private var progressAnimator: ValueAnimator? = null

    fun checkVipStatus() {
        view?.run {
            with(ctx.appSettingsModel) {
                if (!didRemoveAds && !didCheckVipStatus) {
                    iapInteractor.checkVipStatus()
                }
            }
        }
    }

    fun getRemoteConfiguration(activity: AppCompatActivity) {
        view?.also {
        //    firebaseRemoteInteractor.fetchConfig(activity)
        }
    }

    fun startProgressAnimator(activity: AppCompatActivity, progressBar: ContentLoadingProgressBar) {
        view?.also { v ->
            delayBeforeDoSomething(DELAY_BEFORE_LOADING_PROGRESS_BAR) {
                val progressWidth = progressBar.width
                progressBar.max = progressWidth

                progressAnimator = ValueAnimator.ofInt(0, progressWidth).apply {
                    interpolator = LinearInterpolator()
                    startDelay = 0
                    duration = PROGRESS_BAR_DURATION
                    addUpdateListener { valueAnimator ->
                        valueAnimator?.run {
                            val progress = animatedValue as Int
                            progressBar.progress = progress
                            if (progress == progressBar.max) {
                                v.openHomePage()
                                /*if (activity.appSettingsModel.isFirstTimeAppOpened) {
                                    v.openIntroPage()
                                } else {
                                    v.openHomePage()
                                }*/
                                v.close()
                            }
                        }
                    }
                    currentPlayTime = 0
                    start()
                }
            }
        }
    }

    fun removeProgressAnimator() {
        progressAnimator?.run {
            removeAllUpdateListeners()
            progressAnimator = null
        }
    }
}