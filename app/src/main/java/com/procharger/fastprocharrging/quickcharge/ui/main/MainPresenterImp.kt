package com.procharger.fastprocharrging.quickcharge.ui.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.procharger.fastprocharrging.quickcharge.common.extension.addToCompositeDisposable
import com.procharger.fastprocharrging.quickcharge.common.extension.applyComputationWithAndroidMainThread
import com.procharger.fastprocharrging.quickcharge.common.util.RxBus
import com.procharger.fastprocharrging.quickcharge.data.interactor.IapInteractor
import com.procharger.fastprocharrging.quickcharge.ui.base.BasePresenterImp

class MainPresenterImp(ctx: Context) : BasePresenterImp<MainView>(ctx) {

    private val iapInteractor by lazy { IapInteractor(ctx) }

    fun removeAds(activity: AppCompatActivity) {
        view?.also { v ->
            if (networkIsAvailable()) {
                iapInteractor.removeAds(activity)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun listenAppSettingsChanged() {
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