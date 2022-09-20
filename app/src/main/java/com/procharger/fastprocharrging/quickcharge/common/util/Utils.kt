package com.procharger.fastprocharrging.quickcharge.common.util

import android.content.*
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.*
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

class Utils() {
    fun checkJunk() {}

    companion object {
        fun dp2px(resources: Resources, f: Float): Float {
            return f * resources.displayMetrics.density + 0.5f
        }

        fun sp2px(resources: Resources, f: Float): Float {
            return f * resources.displayMetrics.scaledDensity
        }

        fun checkLockedItem(context: Context, str: String): Boolean {
            val locked = PreferAppList().getLocked(context)
            if (locked != null) {
                for (equals: String in locked) {
                    if (equals == str) {
                        return true
                    }
                }
            }
            return false
        }

        fun isUserApp(applicationInfo: ApplicationInfo): Boolean {
            return (applicationInfo.flags and 129) == 0
        }

        val cpuTemp: Float
            get() {
                try {
                    val exec = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
                    exec.waitFor()
                    val readLine = BufferedReader(InputStreamReader(exec.inputStream)).readLine()
                    Log.i("cpu Temp", readLine + "\t")
                    return readLine.toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return 0.0f
                }
            }
        val totalRam: Long
            get() {
                try {
                    val bufferedReader = BufferedReader(FileReader("/proc/meminfo"), 8192)
                    val readLine = bufferedReader.readLine()
                    val split = readLine.split("\\s+").toTypedArray()
                    for (str: String in split) {
                        Log.i(readLine, str + "\t")
                    }
                    val intValue = (Integer.valueOf(split[1]).toInt() * 1024).toLong()
                    bufferedReader.close()
                    return intValue
                } catch (unused: IOException) {
                    return -1
                }
            }
        val totalRAM: Long
            get() {
                var j: Long
                DecimalFormat("#.##")
                try {
                    val randomAccessFile = RandomAccessFile("/proc/meminfo", "r")
                    val matcher = Pattern.compile("(\\d+)").matcher(randomAccessFile.readLine())
                    var str: String? = ""
                    while (matcher.find()) {
                        str = matcher.group(1)
                    }
                    randomAccessFile.close()
                    j = Integer.valueOf(str).toInt().toLong()
                } catch (e: IOException) {
                    e.printStackTrace()
                    j = 0
                }
                return j * 1024
            }

        fun isScreenOn(context: Context): Boolean {
            return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn
        }

        fun getChargeStatus(context: Context): Boolean {
            val intExtra = context.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )!!
                .getIntExtra(NotificationCompat.CATEGORY_STATUS, -1)
            return intExtra == 2 || intExtra == 5
        }

        fun getChargeFull(context: Context): Boolean {
            return context.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )!!
                .getIntExtra(NotificationCompat.CATEGORY_STATUS, -1) == 5
        }

        fun getChargeType(context: Context): String {
            val intExtra = context.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )!!
                .getIntExtra("plugged", -1)
            var z = false
            val z2 = intExtra == 2
            if (intExtra == 1) {
                z = true
            }
            if (z2) {
                return "USB"
            }
            if (z) {
            }
            return "AC"
        }

        fun getTempleCpu(context: Context): Int {
            return context.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )!!
                .getIntExtra("temperature", -1)
        }

        fun getBatteryLevel(context: Context): Int {
            try {
                val registerReceiver = context.applicationContext.registerReceiver(
                    null as BroadcastReceiver?,
                    IntentFilter("android.intent.action.BATTERY_CHANGED")
                )
                val intExtra = registerReceiver!!.getIntExtra("level", -1)
                val intExtra2 = registerReceiver.getIntExtra("scale", -1)
                return if (intExtra == -1 || intExtra2 == -1) {
                    50
                } else (((intExtra.toFloat()) / (intExtra2.toFloat())) * 100.0f).toInt()
            } catch (unused: Exception) {
                return 50
            }
        }
    }
}