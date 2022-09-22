package com.sh.entertainment.fastcharge.common.util


import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.TextView
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.setOnSafeClickListener
import com.sh.entertainment.fastcharge.common.extension.visible

object DialogUtil {

    fun showConfirmationDialog(
        context: Context?,
        textTitle: Any? = null,
        textMessage: Any,
        textOk: Any = context?.getString(R.string.ok) ?: "",
        textCancel: Any? = null,
        okListener: (() -> Unit)? = null,
        cancelListener: (() -> Unit)? = null,
        cancelable: Boolean = false
    ) {
        context?.run {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.setContentView(R.layout.dialog_confirmation)
            dialog.setCancelable(cancelable)

            val lblTitle = dialog.findViewById<TextView>(R.id.lbl_title)
            val lblMessage = dialog.findViewById<TextView>(R.id.lbl_message)
            val btnOk = dialog.findViewById<TextView>(R.id.btn_ok)
            val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)

            textTitle?.let {
                lblTitle.visible()
                lblTitle.text = when (it) {
                    is String -> it
                    is CharSequence -> it
                    is Int -> context.getString(it)
                    else -> ""
                }
            }

            lblMessage.text = when (textMessage) {
                is String -> textMessage
                is CharSequence -> textMessage
                is Int -> context.getString(textMessage)
                else -> ""
            }

            btnOk.text = when (textOk) {
                is String -> textOk
                is CharSequence -> textOk
                is Int -> context.getString(textOk)
                else -> ""
            }
            btnOk.setOnSafeClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                    okListener?.invoke()
                }
            }

            val strCancel = when (textCancel) {
                is String -> textCancel
                is CharSequence -> textCancel
                is Int -> context.getString(textCancel)
                else -> ""
            }
            if (strCancel.isEmpty() || strCancel.isBlank()) {
                btnCancel.visibility = View.GONE
            } else {
                btnCancel.text = strCancel
                btnCancel.setOnSafeClickListener {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        cancelListener?.invoke()
                    }
                }
            }

            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }
}