package com.sh.entertainment.fastcharge.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.sh.entertainment.fastcharge.common.extension.addToCompositeDisposable
import com.sh.entertainment.fastcharge.common.extension.applyComputationWithAndroidMainThread
import com.sh.entertainment.fastcharge.common.util.RxBus
import com.sh.entertainment.fastcharge.data.interactor.IapInteractor
import com.sh.entertainment.fastcharge.ui.base.BasePresenterImp

class SettingsPresenterImp(private val ctx: Context) : BasePresenterImp<SettingsView>(ctx) {

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

    fun openGpsSettings(activity: AppCompatActivity, requestCode: Int) {
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).run {
            activity.startActivityForResult(this, requestCode)
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