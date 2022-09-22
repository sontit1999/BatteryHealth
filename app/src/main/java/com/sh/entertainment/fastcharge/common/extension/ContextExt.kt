package com.sh.entertainment.fastcharge.common.extension

import android.app.Dialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sh.entertainment.fastcharge.BuildConfig
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.DialogUtil
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.model.AdsConfigModel
import com.sh.entertainment.fastcharge.data.model.AppSettingsModel
import java.util.*

inline val Context.ctx: Context
    get() = this

inline val Context.sharedPref: SharedPreferences
    get() = MyApplication.instance.sharedPref

inline val Context.appSettingsModel: AppSettingsModel
    get() = MyApplication.instance.appSettingsModel

inline val Context.adsConfigModel: AdsConfigModel
    get() = MyApplication.instance.adsConfigModel

inline val Context.manufacturer: String
    get() = Build.MANUFACTURER

inline val Context.isXiaomiDevice: Boolean
    get() = manufacturer.equals("Xiaomi", true)

inline val Context.isSamsungDevice: Boolean
    get() = manufacturer.equals("Samsung", true)

inline val Context.isOppoDevice: Boolean
    get() = manufacturer.equals("Oppo", true)

inline val Context.isVivoDevice: Boolean
    get() = manufacturer.equals("Vivo", true)

inline val Context.isAutoRotationEnabled: Boolean
    get() = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1

inline val Context.isAutoSyncEnabled: Boolean
    get() = ContentResolver.getMasterSyncAutomatically()

inline val Context.isWifiEnabled: Boolean
    get() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager?.isWifiEnabled ?: false
    }

inline val Context.isBluetoothEnabled: Boolean
    get() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled ?: false
    }

fun Context.shouldShowAds(): Boolean {
    return adsConfigModel.isAdsEnabled && !appSettingsModel.didRemoveAds
}

fun Context.shouldShowAdsRemovalFeature(): Boolean {
    return shouldShowAds() && adsConfigModel.isAdsRemovalEnabled
}

fun Context.networkIsConnected(): Boolean {
    try {
        val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return conMgr?.let {
            return if (PermissionUtil.isApi29orHigher()) {
                val capabilities = it.getNetworkCapabilities(it.activeNetwork)
                capabilities?.run {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: false
            } else {
                it.activeNetworkInfo?.isConnected ?: false
            }
        } ?: false
    } catch (e: Exception) {
        logE("$e")
    }

    return false
}

fun Context.canWriteSettings(): Boolean {
    return (PermissionUtil.isApi23orHigher() && Settings.System.canWrite(this))
            || !PermissionUtil.isApi23orHigher()
}

fun Context.requestWriteSettingsPermission(requestFrom: Any, requestCode: Int) {
    if (PermissionUtil.isApi23orHigher()) {
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${ctx.packageName}")
        }.run {
            when (requestFrom) {
                is AppCompatActivity -> {
                    requestFrom.startActivityForResult(this, requestCode)
                }
                is Fragment -> {
                    requestFrom.startActivityForResult(this, requestCode)
                }
            }
        }
    }
}

fun Context.canDrawOverlay(): Boolean {
    return PermissionUtil.isApi23orHigher() && Settings.canDrawOverlays(this)
}

fun Context.requestDrawOverlayPermission(requestFrom: Any, requestCode: Int) {
    if (PermissionUtil.isApi23orHigher()) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        when (requestFrom) {
            is AppCompatActivity -> {
                requestFrom.startActivityForResult(intent, requestCode)
            }
            is Fragment -> {
                requestFrom.startActivityForResult(intent, requestCode)
            }
        }
    }
}

fun Context.getInstalledApps(): List<ApplicationInfo> {
    return try {
        packageManager.getInstalledApplications(0).filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 1 && it.packageName != packageName
        }
    } catch (e: java.lang.Exception) {
        listOf()
    }
}

fun Context.requestAppUsageAccessPermission(requestFrom: Any, requestCode: Int) {
    if (PermissionUtil.isApi23orHigher()) {
        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:$packageName")
        )
        when (requestFrom) {
            is AppCompatActivity -> {
                requestFrom.startActivityForResult(intent, requestCode)
            }
            is Fragment -> {
                requestFrom.startActivityForResult(intent, requestCode)
            }
        }
    }
}

fun Context.getAppUsageStatsList(): List<UsageStats> {
    if (PermissionUtil.isApi23orHigher()) {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        usageStatsManager?.let {
            val cal = Calendar.getInstance()
            val endTime = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, -1)
            val startTime = cal.timeInMillis
            return it.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        }
    }

    return emptyList()
}

fun Context.toggleAutoRotation(value: Int): Boolean {
    return Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, value)
}

fun Context.toggleAutoSync(enable: Boolean) {
    ContentResolver.setMasterSyncAutomatically(enable)
}

fun Context.toggleWifi(enable: Boolean) {
    if (!PermissionUtil.isApi29orHigher()) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        wifiManager?.isWifiEnabled = enable
    }
}

fun Context.toggleBluetooth(enable: Boolean): Boolean {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    return if (enable) {
        bluetoothAdapter?.enable()
    } else {
        bluetoothAdapter?.disable()
    } ?: false
}

fun Context.openOtherPermissionsPageOnXiaomiDevice() {
    try {
        Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            putExtra("extra_pkgname", packageName)
        }.run {
            startActivity(this)
        }
    } catch (e: Exception) {
    }
}

fun Context.showRateAppDialog(showCheckBox: Boolean = false, cancelListener: (() -> Unit)? = null) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(R.color.transparent)
        setContentView(R.layout.dialog_rate_app)
        setCancelable(true)

        //val ltvStar = findViewById<LottieAnimationView>(R.id.ltv_star)
        val btnNotNow = findViewById<TextView>(R.id.btn_not_now)
        val btnRate = findViewById<TextView>(R.id.btn_rate)
        val ckbNotShow = findViewById<CheckBox>(R.id.ckb_dont_show_again)

        btnNotNow.setOnSafeClickListener {
            // Dismiss dialog
            //ltvStar.cancelAnimation()
            dismiss()

            cancelListener?.invoke()
        }

        btnRate.setOnSafeClickListener {
            // Dismiss dialog
            //ltvStar.cancelAnimation()
            dismiss()

            // Open app on Play Store
            CommonUtil.openAppInPlayStore(ctx)

            // Save rate button clicked times
            appSettingsModel.run {
                if (clickedRateButtonTimes < 2) {
                    clickedRateButtonTimes += 1
                    CommonUtil.saveAppSettingsModel(ctx, this)
                }
            }
        }

        ckbNotShow.setOnCheckedChangeListener { _, isChecked ->
            // Save user option
            appSettingsModel.run {
                dontShowRateDialogAgain = isChecked
                CommonUtil.saveAppSettingsModel(ctx, this)
            }

            // Dismiss dialog
            //ltvStar.cancelAnimation()
            dismiss()
        }

        if (showCheckBox) {
            ckbNotShow.visible()
        }

        setOnCancelListener {
            // ltvStar.cancelAnimation()
        }

        if (!isShowing) {
            show()
        }
    }
}

fun Context.showAdsRemovingDialog(okListener: () -> Unit) {
    DialogUtil.showConfirmationDialog(
        this, R.string.title_alert_buy, R.string.message_alert_buy,
        R.string.ok, R.string.cancel, cancelable = false,
        okListener = {
            if (networkIsConnected()) {
                okListener()
            } else {
                toast(R.string.alert_buy_error)
            }
        }
    )
}

fun Context.showDrawOverlayPermissionDescDialog( onOkListener:() -> Unit, onCancelListener:() ->Unit) {
    val message =  getString(R.string.desc_permission_draw_overlay) + "\n" + "\n"+ getString(R.string.guide_access_permission_v5)
    DialogUtil.showConfirmationDialog(
        ctx, R.string.launch_app_when_plugged, message,
        R.string.ok, R.string.cancel,
        okListener = {
            onOkListener.invoke()
            // Request draw overlay permission
        }, cancelListener = {
            onCancelListener.invoke()
        }
    )
}

fun Context.showOtherPermissionsDescXiaomiDialog() {
    DialogUtil.showConfirmationDialog(
        ctx, R.string.launch_app_when_plugged, R.string.desc_other_permissions_on_xiaomi_device,
        R.string.ok, R.string.cancel,
        okListener = {
            // Open other permissions page
            openOtherPermissionsPageOnXiaomiDevice()
        }
    )
}

fun Context.logE(msg: Any?) {
    if (BuildConfig.DEBUG) {
        val strMsg = when (msg) {
            is String -> msg
            else -> msg.toString()
        }
        Log.e(javaClass.simpleName, strMsg)
    }
}

fun Context.toast(msg: Any?, length: Int = Toast.LENGTH_SHORT) {
    val message = when (msg) {
        is Int -> getString(msg)
        is Char -> msg.toString()
        is CharSequence -> msg.toString()
        is String -> msg
        else -> "Error: message type is not supported"
    }
    Toast.makeText(this, message, length).show()
}