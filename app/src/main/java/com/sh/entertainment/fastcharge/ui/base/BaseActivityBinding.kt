package com.sh.entertainment.fastcharge.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivityBinding<T : ViewDataBinding> : AppCompatActivity() {

    open lateinit var dataBinding: T

    abstract fun initializeView()
    abstract fun initializeData()
    abstract fun onClick()

    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            dataBinding = DataBindingUtil.bind(layoutInflater.inflate(layoutId, null))!!
            dataBinding.lifecycleOwner = this
            setContentView(dataBinding.root)
        } catch (e: Exception) {

            finish()
            return
        }
        onViewCreated(savedInstanceState)
    }

    open fun onViewCreated(savedInstanceState: Bundle?) {
        initializeView()
        initializeData()
        onClick()
    }

    override fun onDestroy() {
        if (this::dataBinding.isInitialized)
            dataBinding.unbind()
        super.onDestroy()
    }


}