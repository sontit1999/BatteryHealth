package com.sh.entertainment.fastcharge.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PowerBooterReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        ctx?.run {
            if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
                PowerRestarterService.enqueueWork(ctx)
            }
        }
    }
}