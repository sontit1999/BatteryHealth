package com.entertainment.basemvvmproject.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.entertainment.basemvvmproject.utils.toast
import com.entertainment.demoandroidrikkei.base.ui.BaseFragmentNotRequireViewModel

abstract class BaseFragment<BD : ViewDataBinding, VM : BaseViewModel>(@LayoutRes id: Int) :
    BaseFragmentNotRequireViewModel<BD>(id) {

    private lateinit var viewModel: VM

    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getVM()
    }

    override fun initView(savedInstanceState: Bundle?) {
        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) {
                showLoading(it)
            }

            toastMessage.observe(viewLifecycleOwner){
                if(it.isNotEmpty() && activity!=null){
                    it.toString().toast(requireActivity(),Toast.LENGTH_LONG)
                }

            }
        }


    }

}