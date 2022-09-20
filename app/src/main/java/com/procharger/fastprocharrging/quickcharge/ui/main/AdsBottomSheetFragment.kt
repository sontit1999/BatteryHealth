package com.procharger.fastprocharrging.quickcharge.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.MyApplication
import com.procharger.fastprocharrging.quickcharge.common.extension.ctx
import com.procharger.fastprocharrging.quickcharge.widget.ads.LayoutNativeAd


class AdsBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var btnExit: TextView
    private lateinit var nativeAd: LayoutNativeAd

    private lateinit var onCLickExit: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_ads, container, false)
        btnExit = view.findViewById(R.id.btnCloseApp)
        nativeAd = view.findViewById(R.id.nativeAdView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    public fun onClickExit(onCLickExit: () -> Unit) {
        this.onCLickExit = onCLickExit
    }

    private fun init() {
        if (MyApplication.nativeAdExit == null) {
            dismiss()
            return
        }
        val adView =
            FrameLayout.inflate(ctx, R.layout.layout_native_bottom, null) as UnifiedNativeAdView
        nativeAd.apply {
            populateUnifiedNativeAdView(MyApplication.nativeAdExit!!, adView)
            removeAllViews()
            addView(adView)
        }
        btnExit.setOnClickListener {
            onCLickExit.invoke()
        }
    }

}
