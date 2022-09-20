package com.procharger.fastprocharrging.quickcharge.data.interactor

private const val KEY_IS_ADS_ENABLED = "is_ads_enabled"
private const val KEY_IS_ADS_REMOVAL_ENABLED = "is_ads_removal_enabled"
private const val KEY_IS_BANNER_HOME_ENABLED = "is_banner_home_enabled"
private const val KEY_IS_INTERSTITIAL_OPTIMIZE_RESULT_ENABLED =
    "is_interstitial_optimize_result_enabled"
private const val KEY_AD_ID_BANNER_HOME = "ad_id_banner_home"
private const val KEY_AD_ID_INTERSTITIAL_OPTIMIZE_RESULT = "ad_id_interstitial_optimize_result"
private const val KEY_IS_SHOW_SNAIL_ICON = "is_show_snail_icon"

class FirebaseRemoteInteractor : BaseInteractor() {

//    private val remoteConfig by lazy {
//        Firebase.remoteConfig.apply {
//            remoteConfigSettings {
//                minimumFetchIntervalInSeconds = 1 // 1 hour
//            }.run {
//                setConfigSettingsAsync(this)
//            }
//        }
//    }
//
//    fun fetchConfig(activity: AppCompatActivity, onComplete: (() -> Unit)? = null) {
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener(activity) { task ->
//                if (task.isSuccessful) {
//                    // Fill ads config into ads model
//                    activity.ctx.adsConfigModel.apply {
//                        with(remoteConfig) {
//                            isAdsEnabled = getBoolean(KEY_IS_ADS_ENABLED)
//                            isAdsRemovalEnabled = getBoolean(KEY_IS_ADS_REMOVAL_ENABLED)
//                            isBannerHomeEnabled = getBoolean(KEY_IS_BANNER_HOME_ENABLED)
//                            isInterstitialOptimizeResultEnabled =
//                                getBoolean(KEY_IS_INTERSTITIAL_OPTIMIZE_RESULT_ENABLED)
//                            adIdInterstitialOptimizeResult =
//                                getString(KEY_AD_ID_INTERSTITIAL_OPTIMIZE_RESULT)
//                            adIdBannerHome = getString(KEY_AD_ID_BANNER_HOME)
//                        }
//                    }
//
//                    // Fill app config
//                    activity.ctx.appSettingsModel.apply {
//                        with(remoteConfig) {
//                            isShowSnailIcon = getBoolean(KEY_IS_SHOW_SNAIL_ICON)
//                        }
//                    }
//                }
//                onComplete?.invoke()
//            }
//    }
}