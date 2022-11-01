package com.entertainment.demoandroidrikkei.base.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionInflater
import com.entertainment.basemvvmproject.R
import com.entertainment.basemvvmproject.base.BaseActivity
import com.entertainment.basemvvmproject.base.LoadingDialog


abstract class BaseFragmentNotRequireViewModel<BD : ViewDataBinding>(@LayoutRes id: Int) :
    Fragment(id) {

    private var _binding: BD? = null
    protected val binding: BD
        get() = _binding
            ?: throw IllegalStateException("Cannot access view after view destroyed or before view creation")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        exitTransition = inflater.inflateTransition(R.transition.slide_left)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DataBindingUtil.bind(view)
        _binding?.lifecycleOwner = viewLifecycleOwner

        if (savedInstanceState == null) {
            initView(savedInstanceState)

            setOnClick()

            bindingStateView()

            bindingAction()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            initView(savedInstanceState)

            setOnClick()

            bindingStateView()

            bindingAction()
        }
    }

    open fun setOnClick() {

    }

    open fun initView(savedInstanceState: Bundle?) {

    }

    open fun bindingStateView() {

    }

    open fun bindingAction() {

    }

    protected val isDoubleClick: Boolean
        get() {
            if (activity == null) {
                return false
            }
            return if (activity is BaseActivity<*, *>) {
                (activity as BaseActivity<*, *>?)!!.isDoubleClick
            } else false
        }

    override fun onDestroyView() {
        _binding?.unbind()
        _binding = null
        super.onDestroyView()
    }


    fun showLoading(isShow: Boolean, fragmentManager: FragmentManager? = null) {
        if (isShow) {
            LoadingDialog.show(fragmentManager ?: childFragmentManager)
        } else {
            LoadingDialog.hidden(fragmentManager ?: childFragmentManager)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    fun pushFragment(containerId: Int, fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().add(containerId, fragment, tag)
            .addToBackStack(tag).commit()
    }

}