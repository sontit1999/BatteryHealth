package com.sh.entertainment.fastcharge.ui.settings

import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import com.sh.entertainment.fastcharge.ui.base.BaseView

interface SettingsView : BaseView {
    fun onAppSettingsChanged(model: AppSettingsModel)
}