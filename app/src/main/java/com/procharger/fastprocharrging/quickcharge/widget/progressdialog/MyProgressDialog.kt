package com.procharger.fastprocharrging.quickcharge.widget.progressdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.extension.logE
import com.procharger.fastprocharrging.quickcharge.common.extension.visible

class MyProgressDialog(private val ctx: Context) : Dialog(ctx) {

    private val lblMsg by lazy { findViewById<TextView>(R.id.lbl_msg) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(R.drawable.bg_black_transparent)
        setContentView(R.layout.layout_progressbar)
    }

    fun setMessage(res: Any) {
        try {
            val msg = when (res) {
                is String -> res
                is Int -> context.getString(res)
                else -> res.toString()
            }

            lblMsg.visible()
            lblMsg.text = msg
        } catch (e: Exception) {
            ctx.logE("#setMessage() should be called after calling #show()")
        }
    }
}
