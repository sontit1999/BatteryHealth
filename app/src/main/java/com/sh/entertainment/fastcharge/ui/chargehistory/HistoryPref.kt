package com.sh.entertainment.fastcharge.ui.chargehistory

import android.content.Context
import java.util.*

object HistoryPref {
    const val DEFAULT_LEVEL = -1
    private const val HISTORY_PREF = "history_info" + -1962412679
    const val NUMBER_POINT_IN_PER_4_HOUR = 4
    fun putLevel(context: Context, i: Int) {
        val instance = Calendar.getInstance()
        val instance2 = Calendar.getInstance()
        val i2 = instance[12]
        if (i2 <= 3 || i2 >= 57) {
            if (i2 >= 57) {
                instance.add(11, 1)
            }
            val i3 = instance[5]
            val i4 = instance[11]
            putTimeNow(context, i3, i4, i)
            if (!context.getSharedPreferences(HISTORY_PREF, 0).contains(getKeyFromTime(i3, i4))) {
                putLevel(context, i3, i4, i)
                return
            }
            return
        }
        instance.add(11, 1)
        putTimeNow(context, instance[5], instance[11], i)
        context.getSharedPreferences(HISTORY_PREF, 0)
        instance2.add(11, -2)
        removeLevel(context, getKeyFromTimeNow(instance2[5], instance2[11]))
    }

    fun putTimeNow(context: Context, i: Int, i2: Int, i3: Int) {
        val edit = context.getSharedPreferences(HISTORY_PREF, 0).edit()
        edit.putInt("bat_time_now_" + i + "_" + i2, i3)
        edit.apply()
    }

    fun putLevel(context: Context, i: Int, i2: Int, i3: Int) {
        val edit = context.getSharedPreferences(HISTORY_PREF, 0).edit()
        edit.putInt("bat_time_" + i + "_" + i2, i3)
        edit.apply()
    }

    fun getLevel(context: Context, str: String?): Int {
        return context.getSharedPreferences(HISTORY_PREF, 0).getInt(str, -1)
    }

    fun removeLevel(context: Context, str: String?) {
        context.getSharedPreferences(HISTORY_PREF, 0).edit().remove(str).apply()
    }

    fun getKeyFromTime(i: Int, i2: Int): String {
        return "bat_time_" + i + "_" + i2
    }

    fun getKeyFromTimeNow(i: Int, i2: Int): String {
        return "bat_time_now_" + i + "_" + i2
    }
}