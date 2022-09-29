package com.sh.entertainment.fastcharge.ui.dialog

import android.content.Intent
import android.view.ViewGroup
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.databinding.DialogCongratulationBinding
import com.sh.entertainment.fastcharge.ui.base.AppConfig
import com.sh.entertainment.fastcharge.ui.base.BaseDialogFragment
import com.sh.entertainment.fastcharge.ui.battery.BatteryActivity
import com.sh.entertainment.fastcharge.ui.booster.BoosterActivity
import com.sh.entertainment.fastcharge.ui.cool.CoolerActivity

class CongratulationDialog : BaseDialogFragment<DialogCongratulationBinding>() {

    var onClickClose: (() -> Unit)? = null

    override val layoutId: Int
        get() = R.layout.dialog_congratulation

    override fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
            onClickClose?.invoke()
        }

        binding.bgCooler.setOnClickListener {
            dismiss()
            requireActivity().startActivity(Intent(requireContext(), CoolerActivity::class.java))
        }

        binding.bgBooster.setOnClickListener {
            dismiss()
            requireActivity().startActivity(Intent(requireContext(), BoosterActivity::class.java))
        }

        binding.bgBatterySaved.setOnClickListener {
            dismiss()
            requireActivity().startActivity(Intent(requireContext(), BatteryActivity::class.java))
        }

        AdsManager.showNativeAd(requireContext(), binding.nativeAdDialog, AdsManager.NATIVE_AD_KEY)
    }

    override fun onResume() {
        super.onResume()
        setLayout((AppConfig.displayMetrics.widthPixels), ViewGroup.LayoutParams.MATCH_PARENT)
    }

}