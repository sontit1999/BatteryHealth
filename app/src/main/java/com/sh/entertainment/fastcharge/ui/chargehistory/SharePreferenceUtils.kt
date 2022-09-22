package com.sh.entertainment.fastcharge.ui.chargehistory

import android.content.Context
import android.content.SharedPreferences
import com.google.ads.AdRequest

class SharePreferenceUtils private constructor(context: Context) {
    private val editor: SharedPreferences.Editor
    private val pre: SharedPreferences

    fun saveLanguageChange(z: Boolean) {
        editor.putBoolean("LanguageChange", z)
        editor.commit()
    }

    val languageChange: Boolean
        get() = java.lang.Boolean.valueOf(pre.getBoolean("LanguageChange", false))


    val firstRun: Boolean
        get() {
            val z = pre.getBoolean("first_run_app", true)
            if (z) {
                editor.putBoolean("first_run_app", false)
                editor.commit()
            }
            return z
        }

    var timeIn: Long
        get() = pre.getLong("TimeIn", 0)
        set(j) {
            editor.putLong("TimeIn", j)
            editor.commit()
        }
    var timeOut: Long
        get() = pre.getLong("TimeOut", 0)
        set(j) {
            editor.putLong("TimeOut", j)
            editor.commit()
        }
    var time: Long
        get() = pre.getLong("Time", 0)
        set(j) {
            editor.putLong("Time", j)
            editor.commit()
        }
    val enable: Boolean
        get() = pre.getBoolean("Enable", false)
    var chargeNormal: Long
        get() = pre.getLong("ChargeNormal", 0)
        set(j) {
            editor.putLong("ChargeNormal", j)
            editor.commit()
        }
    var chargeHealthy: Long
        get() = pre.getLong("ChargeHealthy", 0)
        set(j) {
            editor.putLong("ChargeHealthy", j)
            editor.commit()
        }
    var chargeOver: Long
        get() = pre.getLong("ChargeOver", 0)
        set(j) {
            editor.putLong("ChargeOver", j)
            editor.commit()
        }
    var chargeFull: String?
        get() = pre.getString("TimeFull", null as String?)
        set(str) {
            editor.putString("TimeFull", str)
            editor.commit()
        }
    var chargeType: String?
        get() = pre.getString("ChargeType", null as String?)
        set(str) {
            editor.putString("ChargeType", str)
            editor.commit()
        }

    fun setDayGetAdmod() {
        editor.putLong("dayGetAdmod", System.currentTimeMillis())
    }

    val dayGetAdmod: Long
        get() = java.lang.Long.valueOf(pre.getLong("dayGetAdmod", 0))
    var levelIn: Long
        get() = pre.getLong("LevelIn", 0)
        set(j) {
            editor.putLong("LevelIn", j)
            editor.commit()
        }
    var timeCharge: Long
        get() = pre.getLong("TimeCharge", 0)
        set(j) {
            editor.putLong("TimeCharge", j)
            editor.commit()
        }
    var chargeQuantity: Long
        get() = pre.getLong("ChargeQuantity", 0)
        set(j) {
            editor.putLong("ChargeQuantity", j)
            editor.commit()
        }
    var killApp: Boolean
        get() = pre.getBoolean("KillApp", true)
        set(z) {
            editor.putBoolean("KillApp", z)
            editor.commit()
        }
    var wifiStatus: Boolean
        get() = pre.getBoolean("WifiStatus", true)
        set(z) {
            editor.putBoolean("WifiStatus", z)
            editor.commit()
        }
    var wifiName: String?
        get() = pre.getString("WifiName", "")
        set(str) {
            editor.putString("WifiName", str)
            editor.commit()
        }
    var autoBrightness: Boolean
        get() = pre.getBoolean("AutoBrightness", true)
        set(z) {
            editor.putBoolean("AutoBrightness", z)
            editor.commit()
        }
    var bluetoothStatus: Boolean
        get() = pre.getBoolean("BluetoothStatus", true)
        set(z) {
            editor.putBoolean("BluetoothStatus", z)
            editor.commit()
        }
    val autoSync: Boolean
        get() = pre.getBoolean("AutoSync", true)

    fun setAutoSyncs(z: Boolean) {
        editor.putBoolean("AutoSync", z)
        editor.commit()
    }

    var autoRunSaverMode: Boolean
        get() = pre.getBoolean("AutoRunSaverMode", false)
        set(z) {
            editor.putBoolean("AutoRunSaverMode", z)
            editor.commit()
        }
    var add: Boolean
        get() = pre.getBoolean("AddBatteryPlan", true)
        set(z) {
            editor.putBoolean("AddBatteryPlan", z)
            editor.commit()
        }
    var postion: Int
        get() = pre.getInt("Postion", 2)
        set(i) {
            editor.putInt("Postion", i)
            editor.commit()
        }
    var typeMode: Int
        get() = pre.getInt("TypeMode", 2)
        set(i) {
            editor.putInt("TypeMode", i)
            editor.commit()
        }
    var levelCheck: Boolean
        get() = pre.getBoolean("LevelCheck", true)
        set(z) {
            editor.putBoolean("LevelCheck", z)
            editor.commit()
        }
    var timeOn: Int
        get() = pre.getInt("TimeOn", 800)
        set(i) {
            editor.putInt("TimeOn", i)
            editor.commit()
        }
    var timeOff: Int
        get() = pre.getInt("TimeOff", 2300)
        set(i) {
            editor.putInt("TimeOff", i)
            editor.commit()
        }
    var smartMode: Boolean
        get() = pre.getBoolean("SmartMode", false)
        set(z) {
            editor.putBoolean("SmartMode", z)
            editor.commit()
        }
    var fsWifi: Boolean
        get() = pre.getBoolean("FsWifi", true)
        set(z) {
            editor.putBoolean("FsWifi", z)
            editor.commit()
        }
    var fsBluetooth: Boolean
        get() = pre.getBoolean("FsBluetooth", true)
        set(z) {
            editor.putBoolean("FsBluetooth", z)
            editor.commit()
        }
    var fsAutoSync: Boolean
        get() = pre.getBoolean("FsAutoSync", false)
        set(z) {
            editor.putBoolean("FsAutoSync", z)
            editor.commit()
        }
    var fsAutoRun: Boolean
        get() = pre.getBoolean("FsAutoRun", false)
        set(z) {
            editor.putBoolean("FsAutoRun", z)
            editor.commit()
        }
    var fsAutoBrightness: Boolean
        get() = pre.getBoolean("FsAutoBrightness", true)
        set(z) {
            editor.putBoolean("FsAutoBrightness", z)
            editor.commit()
        }
    var batterySaveModeIndex: Int
        get() = pre.getInt("BatterySaveModeIndex", 0)
        set(i) {
            editor.putInt("BatterySaveModeIndex", i)
            editor.commit()
        }
    var saveLevel: Int
        get() = pre.getInt("SaveLevel", 30)
        set(i) {
            editor.putInt("SaveLevel", i)
            editor.commit()
        }
    var dndStart: Int
        get() = pre.getInt("DndStart", 2200)
        set(i) {
            editor.putInt("DndStart", i)
            editor.commit()
        }
    var dndStop: Int
        get() = pre.getInt("DndStop", 800)
        set(i) {
            editor.putInt("DndStop", i)
            editor.commit()
        }
    var dnd: Boolean
        get() = pre.getBoolean("Dnd", true)
        set(z) {
            editor.putBoolean("Dnd", z)
            editor.commit()
        }
    var chargeFullReminder: Boolean
        get() = pre.getBoolean("ChargeFullReminder", true)
        set(z) {
            editor.putBoolean("ChargeFullReminder", z)
            editor.commit()
        }
    var chargeFullReminderTime: Long
        get() = pre.getLong("ChargeFullReminderTime", 0)
        set(j) {
            editor.putLong("ChargeFullReminderTime", j)
            editor.commit()
        }
    var lowBatteryReminder: Boolean
        get() = pre.getBoolean("LowBatteryReminder", true)
        set(z) {
            editor.putBoolean("LowBatteryReminder", z)
            editor.commit()
        }
    var tempFormat: Boolean
        get() = pre.getBoolean("TempFormat", true)
        set(z) {
            editor.putBoolean("TempFormat", z)
            editor.commit()
        }
    var coolDownReminder: Boolean
        get() = pre.getBoolean("CoolDownReminder", true)
        set(z) {
            editor.putBoolean("CoolDownReminder", z)
            editor.commit()
        }
    var coolNotification: Boolean
        get() = pre.getBoolean("CoolNotification", false)
        set(z) {
            editor.putBoolean("CoolNotification", z)
            editor.commit()
        }
    val boostReminder: Boolean
        get() = pre.getBoolean("BoostReminder", true)

    fun setBoostRemindert(z: Boolean) {
        editor.putBoolean("BoostReminder", z)
        editor.commit()
    }

    var totalJunk: Long
        get() = pre.getLong("TotalJunk", 0)
        set(j) {
            editor.putLong("TotalJunk", j)
            editor.commit()
        }
    var optimizeTime: Long
        get() = pre.getLong("OptimizeTime", 0)
        set(j) {
            editor.putLong("OptimizeTime", j)
            editor.commit()
        }
    var coolerTime: Long
        get() = pre.getLong("CoolerTime", 0)
        set(j) {
            editor.putLong("CoolerTime", j)
            editor.commit()
        }
    var boostTime: Long
        get() = pre.getLong("BoostTime", 0)
        set(j) {
            editor.putLong("BoostTime", j)
            editor.commit()
        }
    var cleanTime: Long
        get() = pre.getLong("CleanTime", 0)
        set(j) {
            editor.putLong("CleanTime", j)
            editor.commit()
        }
    var chargeStatus: Boolean
        get() = pre.getBoolean("ChargeStatus", false)
        set(z) {
            editor.putBoolean("ChargeStatus", z)
            editor.commit()
        }
    var optimizeTimeMain: Long
        get() = pre.getLong("OptimizeTimeMain", 0)
        set(j) {
            editor.putLong("OptimizeTimeMain", j)
            editor.commit()
        }
    var coolerTimeMain: Long
        get() = pre.getLong("CoolerTimeMain", 0)
        set(j) {
            editor.putLong("CoolerTimeMain", j)
            editor.commit()
        }
    var boostTimeMain: Long
        get() = pre.getLong("BoostTimeMain", 0)
        set(j) {
            editor.putLong("BoostTimeMain", j)
            editor.commit()
        }
    var flagAds: Boolean
        get() = pre.getBoolean(AdRequest.LOGTAG, false)
        set(z) {
            editor.putBoolean(AdRequest.LOGTAG, z)
            editor.commit()
        }
    var notification: Boolean
        get() = pre.getBoolean("notification_enable", true)
        set(z) {
            editor.putBoolean("notification_enable", z)
            editor.commit()
        }
    var hideChargeView: Long
        get() = pre.getLong("HideChargeView", 0)
        set(j) {
            editor.putLong("HideChargeView", j)
            editor.commit()
        }
    var statusPer: Boolean
        get() = pre.getBoolean("StatusPer", true)
        set(z) {
            editor.putBoolean("StatusPer", z)
            editor.commit()
        }
    var levelScreenOn: Int
        get() = pre.getInt("LevelScreenOn", 0)
        set(i) {
            editor.putInt("LevelScreenOn", i)
            editor.commit()
        }
    var statusExit: Boolean
        get() = pre.getBoolean("StatusExit", false)
        set(z) {
            editor.putBoolean("StatusExit", z)
            editor.commit()
        }

    companion object {
        private var instance: SharePreferenceUtils? = null
        fun getInstance(context: Context): SharePreferenceUtils {
            if (instance == null) {
                instance = SharePreferenceUtils(context)
            }
            return instance!!
        }
    }

    init {
        val sharedPreferences = context.getSharedPreferences(
            "app_data",
            Context.MODE_MULTI_PROCESS
        )
        pre = sharedPreferences
        editor = sharedPreferences.edit()
    }
}