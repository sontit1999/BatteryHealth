package com.sh.entertainment.fastcharge.ui.main

import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.ui.base.BaseView

interface MainView : BaseView {
    fun onAppSettingsChanged(model: AppSettingsModel)
}