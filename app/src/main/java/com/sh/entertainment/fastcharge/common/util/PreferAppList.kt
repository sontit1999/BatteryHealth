package com.sh.entertainment.fastcharge.common.util

import android.content.Context
import com.google.gson.Gson
import java.util.*

class PreferAppList {
    fun saveLocked(context: Context, list: List<String?>?) {
        val edit = context.getSharedPreferences(MyPREFERENCES, 0).edit()
        edit.putString(WHITE_LIST, Gson().toJson(list as Any?))
        edit.apply()
    }

    fun getLocked(context: Context): ArrayList<String>? {
        val sharedPreferences = context.getSharedPreferences(MyPREFERENCES, 0)
        return if (!sharedPreferences.contains(WHITE_LIST)) {
            null
        } else ArrayList(
            listOf(
                *Gson().fromJson<Array<String?>>(
                    sharedPreferences.getString(
                        WHITE_LIST, null as String?
                    ),
                    Array<String>::class.java
                ) as Array<String?>
            )
        )
    }

    fun removeLocked(context: Context, str: String?) {
        val locked = getLocked(context)
        if (locked != null) {
            locked.remove(str)
            saveLocked(context, locked)
        }
    }

    companion object {
        private const val MyPREFERENCES = "MyPreferences"
        private const val WHITE_LIST = "whitelist"
        fun getInt(context: Context, str: String?): Int {
            return context.getSharedPreferences(MyPREFERENCES, 0).getInt(str, 0)
        }

        fun getString(context: Context, str: String?, str2: String?): String? {
            return context.getSharedPreferences(MyPREFERENCES, 0).getString(str, str2)
        }
    }
}
