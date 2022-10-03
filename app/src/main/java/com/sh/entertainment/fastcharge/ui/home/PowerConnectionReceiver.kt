package com.sh.entertainment.fastcharge.ui.home

import android.app.ActivityManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.extension.appSettingsModel
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.interactor.BoInteractor
import com.sh.entertainment.fastcharge.ui.boconfirmation.OptimizationConfirmActivity
import com.sh.entertainment.fastcharge.ui.chargehistory.SharePreferenceUtils
import java.text.SimpleDateFormat

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            MyApplication.didOptimized = false
            ctx?.run {
                if (appSettingsModel.batteryPercentage != 0f) {
                    // Open app when plugged
                    if (appSettingsModel.isLaunchAppWhenPlugged && isInBackground()) {
                        startActivity(
                            Intent(ctx, OptimizationConfirmActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                }

                val brightness = Settings.System.getInt(
                    ctx.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
                MyApplication.brightnessValue = brightness

                initPowerConnect(this)
            }
        } else if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            ctx?.run {
                // Restore app settings
                Log.d("HaiHT", "restoreState" + ctx.appSettingsModel.isRestoreStateWhenUnplugged)
                if (ctx.appSettingsModel.isRestoreStateWhenUnplugged) {
                    Log.d("HaiHT", "restoreState")

                    try {
                        BoInteractor(ctx).restoreState()
                        handleRestoreBrightness(ctx)
                    } catch (e: Exception) {

                    }
                }

                // Exit app when unplugged
                if (appSettingsModel.isExitAppWhenUnplugged) {
                    sendBroadcast(Intent(com.sh.entertainment.fastcharge.common.Constants.ACTION_EXIT_APP))
                }

                // Reset global battery percentage var
                appSettingsModel.batteryPercentage = 0f

                powerDisConnected(this)
            }
        }
    }

    private fun initPowerConnect(context: Context) {
        SharePreferenceUtils.getInstance(context).chargeType = getChargeType(context)
        SharePreferenceUtils.getInstance(context).levelIn = getBatteryLevel(context).toLong()
        SharePreferenceUtils.getInstance(context).timeIn = System.currentTimeMillis()
        SharePreferenceUtils.getInstance(context).time = 0
    }

    private fun powerDisConnected(context: Context) {
        if (getBatteryLevel(context) == 100) {
            if (SharePreferenceUtils.getInstance(context).time != 0L) {
                val time = SimpleDateFormat("HH:mm, d MMM yyyy").format(
                    java.lang.Long.valueOf(SharePreferenceUtils.getInstance(context).time)
                )
                SharePreferenceUtils.getInstance(context).chargeFull = time
            }
            if (SharePreferenceUtils.getInstance(context).time == 0L
                || System.currentTimeMillis() - SharePreferenceUtils.getInstance(context).timeIn <= 1800000
                || SharePreferenceUtils.getInstance(context).timeIn == 0L) {
                SharePreferenceUtils.getInstance(context).chargeHealthy =SharePreferenceUtils.getInstance(context).chargeHealthy + 1
            } else {
                SharePreferenceUtils.getInstance(context)
                    .chargeOver =SharePreferenceUtils.getInstance(context).chargeOver + 1
            }
        } else {
            SharePreferenceUtils.getInstance(context)
                .chargeNormal =SharePreferenceUtils.getInstance(context).chargeNormal + 1
        }
        SharePreferenceUtils.getInstance(context).chargeQuantity =getBatteryLevel(context).toLong() - SharePreferenceUtils.getInstance(context).levelIn
        if (SharePreferenceUtils.getInstance(context).timeIn != 0L) {
            SharePreferenceUtils.getInstance(context).timeCharge =System.currentTimeMillis() - SharePreferenceUtils.getInstance(context).timeCharge
        }
        SharePreferenceUtils.getInstance(context).levelIn = 0
        SharePreferenceUtils.getInstance(context).timeIn = 0
    }

    //----------------------------------------------------------------------------------------------
    private fun getChargeType(context: Context): String {
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

    private fun getBatteryLevel(context: Context): Int {
        return try {
            val registerReceiver = context.applicationContext.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
            val intExtra = registerReceiver!!.getIntExtra("level", -1)
            val intExtra2 = registerReceiver.getIntExtra("scale", -1)
            if (intExtra == -1 || intExtra2 == -1) {
                50
            } else (intExtra.toFloat() / intExtra2.toFloat() * 100.0f).toInt()
        } catch (unused: java.lang.Exception) {
            50
        }
    }


    private fun handleRestoreBrightness(context: Context) {
        if (context.appSettingsModel.isReduceScreenTimeOut) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            val brightness =
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, MyApplication.brightnessValue
            )
        }
    }

    private fun isInBackground(): Boolean {
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        return runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    private fun showOptimizationConfirmDialog(ctx: Context?) {
        ctx?.run {
            val dialog = Dialog(this).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                window?.setBackgroundDrawableResource(R.drawable.bg_black_transparent)
                setContentView(R.layout.activity_optimization_confirm)
            }

            val window = dialog.window
            val dialogWindowAttributes = window?.attributes
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(dialogWindowAttributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            window?.attributes = layoutParams
            val type = if (PermissionUtil.isApi26orHigher()) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            window?.setType(type)

            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }
}