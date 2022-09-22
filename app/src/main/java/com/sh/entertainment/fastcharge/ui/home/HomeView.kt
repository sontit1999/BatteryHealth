package com.sh.entertainment.fastcharge.ui.home

import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.ui.base.BaseView

interface HomeView : BaseView {

    fun hasOptimizationOptionsSelected(): Boolean

    fun onNoOptimizationOptionsSelectedError()

    fun onOptimizing()

    fun onOptimizationSuccess()

    fun onAppSettingsChanged(model: AppSettingsModel)

    fun showOptimizationDescDialog()

    fun requestWriteSettingsPermission()
}