package com.procharger.fastprocharrging.quickcharge.ui.main

import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseView

interface MainView : BaseView {
    fun onAppSettingsChanged(model: AppSettingsModel)
}