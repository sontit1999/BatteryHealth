package com.sh.entertainment.fastcharge.ui.home

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.RxBus
import com.sh.entertainment.fastcharge.data.interactor.BoInteractor
import com.sh.entertainment.fastcharge.ui.base.BasePresenterImp
import com.skyfishjy.library.RippleBackground
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class HomePresenterImp(private val ctx: Context) : BasePresenterImp<HomeView>(ctx) {

    private val boInteractor by lazy { BoInteractor(ctx) }

    fun optimise(animationView: LottieAnimationView, isCharging: Boolean) {
        view?.also { v ->
            if (v.hasOptimizationOptionsSelected()) {
                if (ctx.appSettingsModel.didShowOptimizationDescDialog) {
                    if (ctx.canWriteSettings()) {
                        animationView.apply {
                            removeAllAnimatorListeners()
                            cancelAnimation()
                            addAnimatorListener(object : AnimatorListener {
                                override fun onAnimationStart(p0: Animator?) {
                                    v.onOptimizing()
                                }

                                override fun onAnimationEnd(p0: Animator?) {
                                    // Start optimizing process
                                    Single.just(1)
                                        .applyIOWithAndroidMainThread()
                                        .subscribe(object : SingleObserver<Int> {
                                            override fun onSubscribe(disposable: Disposable) {
                                                // Manage disposable
                                                disposable.addToCompositeDisposable(
                                                    compositeDisposable
                                                )

                                                // Start optimizing
                                                boInteractor.optimise(isCharging)
                                            }

                                            override fun onSuccess(t: Int) {
                                                // Notify UI
                                                v.onOptimizationSuccess()
                                            }

                                            override fun onError(e: Throwable) {
                                                e.stackTrace
                                            }
                                        })

                                    // Reset animator
                                    animationView.frame = 0
                                }

                                override fun onAnimationCancel(p0: Animator?) {
                                }

                                override fun onAnimationRepeat(p0: Animator?) {
                                }
                            })
                            playAnimation()
                        }
                    } else {
                        v.requestWriteSettingsPermission()
                    }
                } else {
                    if (!ctx.canWriteSettings()) {
                        v.showOptimizationDescDialog()
                    }
                }
            } else {
                v.onNoOptimizationOptionsSelectedError()
            }
        }
    }

    fun optimiseV2(
        animationOptimization: RippleBackground,
        lytListItem: LinearLayout,
        rocketImage: ImageView
    ) {
        view?.also { v ->
            if (v.hasOptimizationOptionsSelected()) {
                if (ctx.appSettingsModel.didShowOptimizationDescDialog) {
                    if (ctx.canWriteSettings()) {
                        animationOptimization.visible()
                        lytListItem.visible()
                        val animationView =
                            AnimationUtils.loadAnimation(ctx, R.anim.rote_charge_anim)
                        rocketImage.startAnimation(animationView)
                        animationView.apply {
                            start()
                            setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(p0: Animation?) {
                                    v.onOptimizing()
                                }

                                override fun onAnimationEnd(p0: Animation?) {
                                }

                                override fun onAnimationRepeat(p0: Animation?) {
                                    TODO("Not yet implemented")
                                }
                            })
                        }
                    } else {
                        v.requestWriteSettingsPermission()
                    }
                } else {
                    if (!ctx.canWriteSettings()) {
                        v.showOptimizationDescDialog()
                    }
                }
            } else {
                v.onNoOptimizationOptionsSelectedError()
            }
        }
    }

    fun showOptimizationDescProcess(processSize: Int, onProcess: (Int) -> Unit) {
        compositeDisposable.clear()
        view?.run {
            Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .applyIOWithAndroidMainThread()
                .doOnNext {
                    onProcess(it.toInt())
                }
                .takeUntil {
                    it.toInt() == processSize - 1
                }
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun listenAppSettingsChanged() {
        compositeDisposable.clear()
        view?.also { v ->
            RxBus.listenAppSettingsChanged()
                .applyComputationWithAndroidMainThread()
                .subscribe {
                    v.onAppSettingsChanged(it)
                }.addToCompositeDisposable(compositeDisposable)
        }
    }

    fun delayBeforeDoing(delayTime: Long, reachedEnd: () -> Unit) {
        view?.also {
            delayBeforeDoSomething(delayTime) {
                reachedEnd.invoke()
            }
        }
    }
}