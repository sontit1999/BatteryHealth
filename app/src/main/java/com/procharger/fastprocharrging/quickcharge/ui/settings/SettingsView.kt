package com.procharger.fastprocharrging.quickcharge.ui.settings

import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseView

interface SettingsView : BaseView {
    fun onAppSettingsChanged(model: AppSettingsModel)
}