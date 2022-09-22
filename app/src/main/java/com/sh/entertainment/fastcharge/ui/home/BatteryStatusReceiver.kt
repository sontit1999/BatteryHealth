package com.sh.entertainment.fastcharge.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.sh.entertainment.fastcharge.common.extension.logE
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.model.BatteryModel

class BatteryStatusReceiver(private val onBatteryChanged: (BatteryModel) -> Unit) :
    BroadcastReceiver() {

    companion object {
        fun register(ctx: Context?, receiver: BatteryStatusReceiver) {
            IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }.run {
                ctx?.registerReceiver(receiver, this)
            }
        }

        fun unregister(ctx: Context?, receiver: BatteryStatusReceiver) {
            try {
                ctx?.unregisterReceiver(receiver)
            } catch (e: Exception) {
                ctx?.logE(e.message.toString())
            }
        }
    }

    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (intent == null) return
        with(intent) {
            // Battery percentage
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct: Float = if (scale != 0) {
                level * 100 / scale.toFloat()
            } else {
                0f
            }

            // Battery status - charging/not charging
            val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            // Battery temperature
            val temperature =
                getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1).toFloat().div(10)

            // Battery voltage
//                val voltage = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).toFloat().div(1000)

            // Battery charger
            val chargePlugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            // Battery health
            val health = getIntExtra(BatteryManager.EXTRA_HEALTH, -1)

            // Battery technology
            val technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

            // Battery capacity
            val batteryManager = if (PermissionUtil.isApi21orHigher()) {
                ctx?.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            } else {
                null
            }
            val chargeCounter = if (PermissionUtil.isApi21orHigher()) {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            } else {
                null
            }
            val capacity = if (PermissionUtil.isApi21orHigher()) {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.let {
                    if (it != 0) {
                        (chargeCounter?.div(it) ?: 0) / 10
                    } else {
                        0
                    }
                }
            } else {
                null
            }

            // Charging current
            /*val currentCharging = if (PermissionUtil.isApi21orHigher()) {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                    ?.div(1000)
            } else {
                null
            }*/

            Log.d("HaiHT", status.toString())
            // Publish battery status changed event
            BatteryModel().apply {
                this.percentage = batteryPct
                this.status = status
                this.temperatureC = temperature
//                    this.voltage = voltage
                this.chargePlugged = chargePlugged
                this.health = health
                this.technology = technology
                this.capacity = capacity
//                    this.currentCharging = currentCharging
            }.run {
                onBatteryChanged(this)
            }
        }
    }
}