package com.sh.entertainment.fastcharge.data.model

class AdsConfigModel : BaseModel() {
    var isAdsRemovalEnabled = false
    var isAdsEnabled = false
    var isBannerHomeEnabled = false
    var isInterstitialOptimizeResultEnabled = false
    var adIdInterstitialOptimizeResult = ""
    var adIdBannerHome = ""
}