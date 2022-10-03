package com.sh.entertainment.fastcharge.data.model

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
    var is_open_app: Boolean = true,
    var keyIntel: String = "ca-app-pub-7254202909466206/1506951650",
    var keyOpenAds: String = "ca-app-pub-7254202909466206/9577013365",
    var keyNative: String = "ca-app-pub-7254202909466206/8427297816",
    var keyNativeExit: String = "ca-app-pub-7254202909466206/8427297816"
)