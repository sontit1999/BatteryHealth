package com.procharger.fastprocharrging.quickcharge.ui.home

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.procharger.fastprocharrging.quickcharge.common.extension.ctx
import com.procharger.fastprocharrging.quickcharge.common.extension.logE

class PowerRestarterService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1000

        fun enqueueWork(ctx: Context) {
            enqueueWork(
                ctx, PowerRestarterService::class.java, JOB_ID,
                Intent(ctx, PowerRestarterService::class.java)
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        logE("onHandleWork")
        PowerConnectionService.start(ctx, true)
    }
}