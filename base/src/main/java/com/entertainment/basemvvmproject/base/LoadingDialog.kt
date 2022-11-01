package com.entertainment.basemvvmproject.base

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.entertainment.basemvvmproject.databinding.LayoutLoadingBinding


class LoadingDialog : DialogFragment() {
    companion object {

        private const val LOADING_TAG = "DefaultLoadingDialog"

        fun show(fragmentManager: FragmentManager) {
            val loadingDialog = fragmentManager.findFragmentByTag(LOADING_TAG) as LoadingDialog?
            if (loadingDialog == null) {
                LoadingDialog().show(fragmentManager, LOADING_TAG)
            }
        }

        fun hidden(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(LOADING_TAG) as LoadingDialog?)?.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context)
        val binding = LayoutLoadingBinding.inflate(layoutInflater)
        arguments?.let {
            if (it.containsKey("IS_SHOW_LOADING_ALERT")) {
                val isShowAlert = it.getBoolean("IS_SHOW_LOADING_ALERT")
                binding.tvTitle.isVisible = isShowAlert
                binding.tvSubTitle.isVisible = isShowAlert
            }
        }
        dialogBuilder.setView(binding.root)
        val dialog = dialogBuilder.create()
        dialogBuilder.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        isCancelable = false
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}