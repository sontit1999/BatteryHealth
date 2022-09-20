package com.procharger.fastprocharrging.quickcharge.ui.base

import android.content.Context
import com.procharger.fastprocharrging.quickcharge.common.extension.addToCompositeDisposable
import com.procharger.fastprocharrging.quickcharge.common.extension.networkIsConnected
import com.procharger.fastprocharrging.quickcharge.widget.progressdialog.MyProgressDialog
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

open class BasePresenterImp<T : BaseView>(private val ctx: Context) : BasePresenter<T>() {

    private val progressDialog: MyProgressDialog by lazy { MyProgressDialog(ctx) }

    protected var view: T? = null
    protected val compositeDisposable by lazy { CompositeDisposable() }

    override fun attachView(view: T) {
        this.view = view
    }

    override fun detachView() {
        view?.onDestroyAds()
        compositeDisposable.clear()
        view = null
    }

    protected fun showProgressDialog(cancelable: Boolean = false) {
        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(cancelable)
            progressDialog.show()
        }
    }

    protected fun dismissProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    protected fun networkIsAvailable(): Boolean {
        return ctx.networkIsConnected()
    }

    protected fun delayBeforeDoSomething(
        delayTime: Long,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        onSuccess: () -> Unit
    ) {
        compositeDisposable.clear()
        view?.also {
            Single.timer(
                delayTime,
                timeUnit,
                AndroidSchedulers.mainThread()
            ).subscribe(object : SingleObserver<Any> {
                override fun onSubscribe(d: Disposable) {
                    d.addToCompositeDisposable(compositeDisposable)
                }

                override fun onSuccess(t: Any) {
                    onSuccess.invoke()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            })
        }
    }
}