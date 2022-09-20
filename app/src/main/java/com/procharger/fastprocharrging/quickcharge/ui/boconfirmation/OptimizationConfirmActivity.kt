package com.procharger.fastprocharrging.quickcharge.ui.boconfirmation

import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.extension.appSettingsModel
import com.procharger.fastprocharrging.quickcharge.common.extension.ctx
import com.procharger.fastprocharrging.quickcharge.common.extension.openActivity
import com.procharger.fastprocharrging.quickcharge.common.extension.setOnSafeClickListener
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseActivity
import com.procharger.fastprocharrging.quickcharge.ui.main.MainActivity

class OptimizationConfirmActivity :
    BaseActivity<OptimizationConfirmView, OptimizationConfirmPresenterImp>(),
    OptimizationConfirmView {

    private val btnNo by lazy { findViewById<TextView>(R.id.btn_no) }
    private val btnYes by lazy { findViewById<TextView>(R.id.btn_yes) }
    private val imgNormalCharge by lazy { findViewById<ImageView>(R.id.img_normal_charge) }

    override fun initView(): OptimizationConfirmView {
        return this
    }

    override fun initPresenter(): OptimizationConfirmPresenterImp {
        return OptimizationConfirmPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_optimization_confirm
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()

        // Fill UI
        with(appSettingsModel) {
            val img = if (isShowSnailIcon) {
                R.drawable.ic_snail
            } else {
                R.drawable.ic_turtle
            }
            imgNormalCharge.setImageResource(img)
        }

        // Listeners
        btnNo.setOnSafeClickListener {
            finish()
        }

        btnYes.setOnSafeClickListener {
            // Open MainActivity
            bundleOf(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_ACTION to com.procharger.fastprocharrging.quickcharge.common.Constants.ACTION_OPTIMIZE).apply {
                openActivity(MainActivity::class.java, this)
            }

            // Close this activity
            finish()
        }
    }
}
