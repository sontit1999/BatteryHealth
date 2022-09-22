package com.sh.entertainment.fastcharge.ui.base

abstract class BasePresenter<T : BaseView> {

    abstract fun attachView(view: T)

    abstract fun detachView()
}