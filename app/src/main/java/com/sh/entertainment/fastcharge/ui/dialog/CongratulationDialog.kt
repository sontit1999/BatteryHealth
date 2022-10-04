package com.sh.entertainment.fastcharge.ui.dialog

import android.util.Log
import android.view.ViewGroup
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.gone
import com.sh.entertainment.fastcharge.common.extension.visible
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.data.model.AdsModel
import com.sh.entertainment.fastcharge.databinding.DialogCongratulationBinding
import com.sh.entertainment.fastcharge.ui.base.AppConfig
import com.sh.entertainment.fastcharge.ui.base.BaseDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CongratulationDialog : BaseDialogFragment<DialogCongratulationBinding>() {

    var onClickClose: (() -> Unit)? = null

    override val layoutId: Int
        get() = R.layout.dialog_congratulation

    override fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
            onClickClose?.invoke()
        }
        if (MyApplication.remoteConfigModel.isEnableAds && MyApplication.remoteConfigModel.is_native_congratulation) {
            AdsManager.showNativeAd(
                requireContext(),
                binding.nativeAdDialog,
                AdsManager.NATIVE_AD_KEY
            )
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdsModelEvent(adsModel: AdsModel) {
        Log.d("HaiHT", "onAdsModelEvent")
        // Do something
        if(adsModel.type == 1) {
            if(adsModel.isShow){
                // hidden native ads
                binding.nativeAdDialog.gone()
            } else {
                // show native ads
                binding.nativeAdDialog.visible()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        setLayout((AppConfig.displayMetrics.widthPixels), ViewGroup.LayoutParams.MATCH_PARENT)
    }

}