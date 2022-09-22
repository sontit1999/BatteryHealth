package com.sh.entertainment.fastcharge.common.util

import com.sh.entertainment.fastcharge.BuildConfig

object PackageUtil {

    fun getAppID(): String {
        return BuildConfig.APPLICATION_ID
    }
}