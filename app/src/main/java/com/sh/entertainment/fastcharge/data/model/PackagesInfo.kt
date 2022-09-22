package com.sh.entertainment.fastcharge.data.model

import android.content.Context

class PackagesInfo(context: Context) {
    var appList: List<*>

    init {
        appList = context.applicationContext.packageManager.getInstalledApplications(0)
    }
}
