package com.entertainment.basemvvmproject.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.demoandroidrikkei.base.ui.BaseActivityNotRequireViewModel


abstract class BaseActivity<BD : ViewDataBinding, VM : BaseViewModel> :
    BaseActivityNotRequireViewModel<BD>() {

    private lateinit var viewModel: VM

    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getVM()

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    fun pushFragment(containerId: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().add(containerId, fragment, tag)
            .addToBackStack(null).commit()
    }

    fun popFragment() {
        supportFragmentManager.popBackStack()
    }

}