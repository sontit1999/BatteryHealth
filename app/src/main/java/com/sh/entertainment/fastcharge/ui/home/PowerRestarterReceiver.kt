package com.sh.entertainment.fastcharge.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PowerRestarterReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        ctx?.run {
            PowerRestarterService.enqueueWork(ctx)
        }
    }
}