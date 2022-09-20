package com.procharger.fastprocharrging.quickcharge.common

import com.procharger.fastprocharrging.quickcharge.BuildConfig

object Constants {
    const val LINK_APP_ON_STORE =
        "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    //const val LINK_POLICY = "https://sites.google.com/view/x2charger"
    const val LINK_POLICY = "https://sites.google.com/view/privacypolicy-masterbattery/"

    const val ACTION_EXIT_APP = "action_exit_app"
    const val ACTION_OPTIMIZE = "action_optimize"

    const val KEY_ACTION = "key_action"
    const val KEY_CHECK_SHOWING_RATE_DIALOG = "key_check_showing_rate_dialog"
}