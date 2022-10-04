package com.sh.entertainment.fastcharge.data.model

data class RemoteConfig(
    var isEnableAds: Boolean = false,
    var timeShowInter: Int = 30,
    var timeShowAdsOpenApp: Int = 60,
    var is_native_home: Boolean = false,
    var is_native_result: Boolean = false,
    var is_native_exit_app: Boolean = false,
    var is_inter_back_info: Boolean = false,
    var is_inter_back_setting: Boolean = false,
    var is_inter_result: Boolean = false,
    var is_open_app: Boolean = false,
    var keyIntel: String = "ca-app-pub-3940256099942544/1033173712",
    var keyOpenAds: String = "ca-app-pub-3940256099942544/3419835294",
    var keyNative: String = "ca-app-pub-3940256099942544/2247696110",
    var keyNativeExit: String = "ca-app-pub-3940256099942544/2247696110",
    var is_native_congratulation: Boolean = false
)