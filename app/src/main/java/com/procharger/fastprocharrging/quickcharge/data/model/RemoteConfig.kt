package com.procharger.fastprocharrging.quickcharge.data.model

data class RemoteConfig(
    var isEnableAds: Boolean = true,
    var timeShowInter: Int = 30,
    var timeShowAdsOpenApp: Int = 60,
    var is_native_home: Boolean = true,
    var is_native_result: Boolean = true,
    var is_native_exit_app: Boolean = true,
    var is_inter_back_info: Boolean = true,
    var is_inter_back_setting: Boolean = true,
    var is_inter_result: Boolean = true,
    var is_open_app: Boolean = true
)