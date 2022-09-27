package com.sh.entertainment.fastcharge.ui.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

open abstract class BaseDialogFragment<VB : ViewDataBinding> : DialogFragment() {

    lateinit var binding: VB

    open var canceledOnTouchOutside: Boolean = false
    open var dim: Float = 0.4f
    protected var mDialog: Dialog? = null
    protected abstract val layoutId: Int
    var mView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BaseDialog(requireContext(), theme)
        try {
            dialog.onBackListener = this::onBackPressed
            dialog.touchOutside = canceledOnTouchOutside
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val mView = dialog.layoutInflater.inflate(layoutId, null)
            dialog.setContentView(mView)
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            val window = dialog.window!!
            window.setGravity(Gravity.CENTER)
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(dim)
            this.mView = mView
            mDialog = dialog
            init(mView)
        } catch (e: Exception) {

        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    abstract fun initView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,layoutId,container,false)
        return binding.root
    }

    override fun onDetach() {
        mDialog = null
        super.onDetach()
        mView = null
    }

    fun setLayout(width: Int, height: Int) {
        mDialog?.window?.setLayout(width, height)
    }

    protected open fun init(view: View) {

    }

    open fun onBackPressed() : Boolean {
        return true
    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed || manager.isStateSaved) return
        try {
            super.show(manager, tag)
        } catch (e: Exception) {

        }
    }

    private class BaseDialog: Dialog {

        var onBackListener: (() -> Boolean)? = null
        var touchOutside: Boolean = false
        constructor(context: Context) : super(context)
        constructor(context: Context, themeResId: Int) : super(context, themeResId)
        constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)
    }
}