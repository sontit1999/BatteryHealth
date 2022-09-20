package com.procharger.fastprocharrging.quickcharge.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager

class BatteryPref private constructor(context: Context?) {
    fun putLevel(context: Context, i: Int) {
        val sharedPreferences = context.getSharedPreferences(BATTERY_PREF, 0)
        if (i != getLevel(context)) {
            sharedPreferences.edit().putInt("level", i).commit()
        }
    }

    @SuppressLint("WrongConstant")
    fun getTimeRemainning(context: Context, i: Int): Int {
        val sharedPreferences = context.getSharedPreferences(BATTERY_PREF, 0)
        (context.getSystemService("power") as PowerManager).isScreenOn
        val i2 = (i.toLong() * sharedPreferences.getLong(
            EXTRA_TIME_REMAIN,
            TIME_REMAIN_DEFAULT
        ) / 60000).toInt()
        if (getLevel(context) == -1) {
            putLevel(context, i)
        } else if (i < getLevel(context)) {
            sharedPreferences.edit().putInt("level", i).commit()
            if (sharedPreferences.getLong(EXTRA_CURENT_TIME1, 0) != 0L && sharedPreferences.getLong(
                    EXTRA_CURENT_TIME2, 0
                ) != 0L
            ) {
                val j = sharedPreferences.getLong(TIMEMAIN1, 0)
                val j2 = TIME_REMAIN_DEFAULT
                val currentTimeMillis = (System.currentTimeMillis() - sharedPreferences.getLong(
                    EXTRA_CURENT_TIME2, System.currentTimeMillis()
                ) + j + j2 + sharedPreferences.getLong(
                    EXTRA_TIME_REMAIN, j2
                )) / 3
                if (currentTimeMillis > TIME_REMAIN_MIN) {
                    if (currentTimeMillis < TIME_REMAIN_MAX) {
                        sharedPreferences.edit().putLong(EXTRA_TIME_REMAIN, currentTimeMillis)
                            .commit()
                    } else {
                        sharedPreferences.edit().putLong(EXTRA_TIME_REMAIN, TIME_REMAIN_MIN)
                            .commit()
                    }
                }
                sharedPreferences.edit().putLong(TIMEMAIN1, j).commit()
                sharedPreferences.edit().putLong(EXTRA_CURENT_TIME2, System.currentTimeMillis())
                    .commit()
            } else if (sharedPreferences.getLong(EXTRA_CURENT_TIME1, 0) == 0L) {
                sharedPreferences.edit().putLong(EXTRA_CURENT_TIME1, System.currentTimeMillis())
                    .commit()
                return i2
            } else if (sharedPreferences.getLong(EXTRA_CURENT_TIME2, 0) == 0L) {
                sharedPreferences.edit().putLong(
                    TIMEMAIN1, System.currentTimeMillis() - sharedPreferences.getLong(
                        EXTRA_CURENT_TIME1, System.currentTimeMillis()
                    )
                ).commit()
                sharedPreferences.edit().putLong(EXTRA_CURENT_TIME2, System.currentTimeMillis())
                    .commit()
            }
        }
        return i2
    }

    fun getTimeChargingUsb(context: Context, i: Int): Int {
        val sharedPreferences = context.getSharedPreferences(BATTERY_PREF, 0)
        val i2 = ((100 - i).toLong() * sharedPreferences.getLong(
            EXTRA_TIME_CHARGING_USB,
            108000
        ) / 60000).toInt()
        if (i > getLevel(context)) {
            sharedPreferences.edit().putInt("level", i).commit()
            val currentTimeMillis = System.currentTimeMillis() - sharedPreferences.getLong(
                EXTRA_CURENT_TIME_CHARGE_USB, System.currentTimeMillis()
            )
            if (currentTimeMillis < TIME_CHARGING_USB_MAX && currentTimeMillis > 72000) {
                sharedPreferences.edit().putLong(EXTRA_TIME_CHARGING_USB, currentTimeMillis)
                    .commit()
            }
        }
        return i2
    }

    fun getTimeChargingAc(context: Context, i: Int): Int {
        val sharedPreferences = context.getSharedPreferences(BATTERY_PREF, 0)
        val i2 = ((100 - i).toLong() * sharedPreferences.getLong(
            EXTRA_TIME_CHARGING_AC,
            72000
        ) / 60000).toInt()
        if (i > getLevel(context)) {
            sharedPreferences.edit().putInt("level", i).commit()
            val currentTimeMillis = System.currentTimeMillis() - sharedPreferences.getLong(
                EXTRA_CURENT_TIME_CHARGE_AC, System.currentTimeMillis()
            )
            if (currentTimeMillis < 108000 && currentTimeMillis > TIME_CHARGING_AC_MIN) {
                sharedPreferences.edit().putLong(EXTRA_TIME_CHARGING_AC, currentTimeMillis).commit()
            }
        }
        return i2
    }

    fun getLevel(context: Context): Int {
        return context.getSharedPreferences(BATTERY_PREF, 0).getInt("level", -1)
    }

    companion object {
        const val BATTERY_PREF = "battery_info" + -1690947680
        const val EXTRA_CURENT_TIME1 = "curenttime1"
        const val EXTRA_CURENT_TIME2 = "curenttime2"
        const val EXTRA_CURENT_TIME_CHARGE_AC = "time_charge_ac"
        const val EXTRA_CURENT_TIME_CHARGE_USB = "time_charge_USB"
        const val EXTRA_LEVEL = "level"
        const val EXTRA_TIME_CHARGING_AC = "timecharging_ac"
        const val EXTRA_TIME_CHARGING_USB = "timecharging_usb"
        const val EXTRA_TIME_REMAIN = "timeremainning"
        const val TIMEMAIN1 = "timemain1"
        const val TIMESCREENOFF = "time_Screen_on"
        const val TIMESCREENON = "time_Screen_on"
        const val TIME_CHARGING_AC_DEFAULT: Long = 72000
        const val TIME_CHARGING_AC_MAX: Long = 108000
        const val TIME_CHARGING_AC_MIN: Long = 36000
        const val TIME_CHARGING_USB_DEFAULT: Long = 108000
        const val TIME_CHARGING_USB_MAX: Long = 180000
        const val TIME_CHARGING_USB_MIN: Long = 72000
        var TIME_REMAIN_DEFAULT: Long = 864000
        const val TIME_REMAIN_MAX: Long = 1440000
        const val TIME_REMAIN_MIN: Long = 720000
        private var batteryPref: BatteryPref? = null
        var mContext: Context? = null
        fun getBatteryCapacity(context: Context?): Double {
            val obj: Any?
            obj = try {
                Class.forName("com.android.internal.os.PowerProfile").getConstructor(
                    *arrayOf<Class<*>>(
                        Context::class.java
                    )
                ).newInstance(*arrayOf<Any?>(context))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            return try {
                (Class.forName("com.android.internal.os.PowerProfile").getMethod(
                    "getAveragePower", *arrayOf<Class<*>>(
                        String::class.java
                    )
                ).invoke(obj, *arrayOf<Any>("battery.capacity")) as Double).toDouble()
            } catch (e2: Exception) {
                e2.printStackTrace()
                java.lang.Double.longBitsToDouble(1)
            }
        }

        fun initilaze(context: Context?): BatteryPref? {
            mContext = context
            if (batteryPref == null) {
                batteryPref = BatteryPref(mContext)
            }
            return batteryPref
        }
    }

    init {
        val round = Math.round(getBatteryCapacity(context)) * 24 / 3
        TIME_REMAIN_DEFAULT = TIME_REMAIN_DEFAULT * Math.round(getBatteryCapacity(context)) / 3200
        val sharedPreferences = context!!.getSharedPreferences(BATTERY_PREF, 0)
        sharedPreferences.edit().putLong(EXTRA_TIME_REMAIN, TIME_REMAIN_DEFAULT).commit()
        if (!sharedPreferences.contains(EXTRA_TIME_REMAIN) || !sharedPreferences.contains(
                EXTRA_CURENT_TIME1
            ) || !sharedPreferences.contains(EXTRA_TIME_CHARGING_AC) || !sharedPreferences.contains(
                EXTRA_TIME_CHARGING_USB
            )
        ) {
            sharedPreferences.edit().putLong(EXTRA_TIME_CHARGING_AC, 72000).commit()
            sharedPreferences.edit().putLong(EXTRA_TIME_CHARGING_USB, 108000).commit()
        }
    }
}