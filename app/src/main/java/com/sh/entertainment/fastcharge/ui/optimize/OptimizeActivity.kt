package com.sh.entertainment.fastcharge.ui.optimize

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.addToCompositeDisposable
import com.sh.entertainment.fastcharge.common.extension.appSettingsModel
import com.sh.entertainment.fastcharge.common.extension.applyIOWithAndroidMainThread
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.interactor.BoInteractor
import com.sh.entertainment.fastcharge.databinding.FragmentOptimizeBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import com.sh.entertainment.fastcharge.ui.dialog.CongratulationDialog
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OptimizeActivity(var isCharging: Boolean = false) :
    BaseActivityBinding<FragmentOptimizeBinding>() {

    private val boInteractor by lazy { BoInteractor(this) }
    protected val compositeDisposable by lazy { CompositeDisposable() }

    private fun bindingAction() {
        dataBinding.animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                val dialogCongratulation = CongratulationDialog()
                dialogCongratulation.onClickClose = {
                    finish()
                }
                dialogCongratulation.show(supportFragmentManager, "dialogCongratulation")
            }

            override fun onAnimationRepeat(animation: Animator) {
                super.onAnimationRepeat(animation)
            }

            override fun onAnimationStart(animation: Animator) {
                updateText()
                Single.just(1)
                    .applyIOWithAndroidMainThread()
                    .subscribe(object : SingleObserver<Int> {
                        override fun onSubscribe(disposable: Disposable) {
                            disposable.addToCompositeDisposable(compositeDisposable)
                            boInteractor.optimise(isCharging)
                        }

                        override fun onSuccess(t: Int) {
                            MyApplication.didOptimized = true
                        }

                        override fun onError(e: Throwable) {
                            e.stackTrace
                        }
                    })

                dataBinding.animationView.frame = 0
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

    private fun updateText() {
        val arrOptimizationDescProcess = arrayListOf<String>().apply {
            appSettingsModel.run {
                if (isCharging) {
                    add(getString(R.string.clean_apps))
                }
                if (isTurnOffBluetooth) {
                    add(getString(R.string.turn_off_bluetooth))
                }
                if (!PermissionUtil.isApi29orHigher() && isTurnOffWifi) {
                    add(getString(R.string.turn_off_wifi))
                }
                if (isTurnOffAutoSync) {
                    add(getString(R.string.turn_off_auto_sync))
                }
                if (isTurnOffScreenRotation) {
                    add(getString(R.string.turn_off_screen_rotation))
                }

                if (isClearRam) {
                    add(getString(R.string.clear_ram))
                }

                if (isCharging && isReduceScreenTimeOut) {
                    add(getString(R.string.reduce_screen_timeout))
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            arrOptimizationDescProcess.forEach {
                delay(300)
                dataBinding.tvDetailOptimize.text = it
            }
        }
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