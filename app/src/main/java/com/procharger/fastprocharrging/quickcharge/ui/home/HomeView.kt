package com.procharger.fastprocharrging.quickcharge.ui.home

import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseView

interface HomeView : BaseView {

    fun hasOptimizationOptionsSelected(): Boolean

    fun onNoOptimizationOptionsSelectedError()

    fun onOptimizing()

    fun onOptimizationSuccess()

    fun onAppSettingsChanged(model: AppSettingsModel)

    fun showOptimizationDescDialog()

    fun requestWriteSettingsPermission()
}