package com.sh.entertainment.fastcharge.ui.dialog

import android.view.ViewGroup
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.databinding.DialogCongratulationBinding
import com.sh.entertainment.fastcharge.ui.base.AppConfig
import com.sh.entertainment.fastcharge.ui.base.BaseDialogFragment

class CongratulationDialog : BaseDialogFragment<DialogCongratulationBinding>() {

    var onClickClose: (() -> Unit)? = null

    override val layoutId: Int
        get() = R.layout.dialog_congratulation

    override fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
            onClickClose?.invoke()
        }
        AdsManager.showNativeAd(requireContext(), binding.nativeAdDialog, AdsManager.NATIVE_AD_KEY)
    }

    override fun onResume() {
        super.onResume()
        setLayout((AppConfig.displayMetrics.widthPixels), ViewGroup.LayoutParams.MATCH_PARENT)
    }

}