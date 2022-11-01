package com.entertainment.basemvvmproject.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding


abstract class BaseBottomSheet<BD : ViewDataBinding, VM : BaseViewModel> :
    BaseBottomSheetNotRequireViewModel<BD>() {

    protected lateinit var viewModel: VM
    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getVM()
    }

}