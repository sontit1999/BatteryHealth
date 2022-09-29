package com.sh.entertainment.fastcharge.ui.home

import android.content.Context
import com.sh.entertainment.fastcharge.common.extension.addToCompositeDisposable
import com.sh.entertainment.fastcharge.common.extension.applyComputationWithAndroidMainThread
import com.sh.entertainment.fastcharge.common.extension.applyIOWithAndroidMainThread
import com.sh.entertainment.fastcharge.common.util.RxBus
import com.sh.entertainment.fastcharge.data.interactor.BoInteractor
import com.sh.entertainment.fastcharge.ui.base.BasePresenterImp
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class HomePresenterImp(private val ctx: Context) : BasePresenterImp<HomeView>(ctx) {

    private val boInteractor by lazy { BoInteractor(ctx) }


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

}