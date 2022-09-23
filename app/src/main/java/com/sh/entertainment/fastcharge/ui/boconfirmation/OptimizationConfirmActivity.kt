package com.sh.entertainment.fastcharge.ui.boconfirmation

import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.appSettingsModel
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.extension.openActivity
import com.sh.entertainment.fastcharge.common.extension.setOnSafeClickListener
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.ui.base.BaseActivity
import com.sh.entertainment.fastcharge.ui.main.MainActivity
import com.sh.entertainment.fastcharge.widget.ads.LayoutNativeAd

class OptimizationConfirmActivity :
    BaseActivity<OptimizationConfirmView, OptimizationConfirmPresenterImp>(),
    OptimizationConfirmView {

    private val btnNo by lazy { findViewById<TextView>(R.id.btn_no) }
    private val btnYes by lazy { findViewById<TextView>(R.id.btn_yes) }
    private val nativeAd by lazy { findViewById<LayoutNativeAd>(R.id.nativeAdViewConfirm) }
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
            bundleOf(com.sh.entertainment.fastcharge.common.Constants.KEY_ACTION to com.sh.entertainment.fastcharge.common.Constants.ACTION_OPTIMIZE).apply {
                openActivity(MainActivity::class.java, this)
            }

            // Close this activity
            finish()
        }

        loadNativeAds()
    }

    private fun loadNativeAds() {
        if(MyApplication.remoteConfigModel.isEnableAds){
            AdsManager.showNativeAd(this,nativeAd,AdsManager.NATIVE_AD_KEY)
        }
    }
}
