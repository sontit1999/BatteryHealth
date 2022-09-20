package com.procharger.fastprocharrging.quickcharge.common.util

import com.procharger.fastprocharrging.quickcharge.BuildConfig

object PackageUtil {

    fun getAppID(): String {
        return BuildConfig.APPLICATION_ID
    }
}