package com.sh.entertainment.fastcharge.data.model

import android.os.BatteryManager

class BatteryModel : BaseModel() {
    var percentage: Float? = null

    var status: Int? = null

    var temperatureC: Float? = null

//    var voltage: Float? = null

    var capacity: Int? = null

//    var currentCharging: Int? = null

    var chargePlugged: Int? = null

    var technology: String? = null

    var health: Int? = null

    val isHealthCold: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_COLD

    val isHealthGood: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_GOOD

    val isHealthDead: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_DEAD

    val isHealthOverHeat: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_OVERHEAT

    val isHealthOverVoltage: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE

    val isHealthUnspecifiedFailure: Boolean
        get() = health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE

    val isPluggedUsb: Boolean
        get() = chargePlugged == BatteryManager.BATTERY_PLUGGED_USB

    val isPluggedAc: Boolean
        get() = chargePlugged == BatteryManager.BATTERY_PLUGGED_AC

    val isPluggedWireless: Boolean
        get() = chargePlugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

    val temperatureF: Float
        get() = (temperatureC?.times(9 / 5f) ?: 0f) + 32

    val isCharging: Boolean
        get() = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    val isBatteryFull: Boolean
        get() = status == BatteryManager.BATTERY_STATUS_FULL
}