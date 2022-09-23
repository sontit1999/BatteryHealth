package com.sh.entertainment.fastcharge.ui.home

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.multidex.BuildConfig
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.*
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.NumberUtil
import com.sh.entertainment.fastcharge.common.util.PermissionUtil
import com.sh.entertainment.fastcharge.data.model.BatteryModel
import com.sh.entertainment.fastcharge.ui.main.ActionHandlerActivity
import com.sh.entertainment.fastcharge.ui.main.MainActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

class PowerConnectionService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val RC_OPTIMIZE = 1

        var isRunning = false
        var chargeBattery = ""

        fun start(ctx: Context, forceStart: Boolean = false) {
            if (shouldStartService(ctx) || forceStart) {
                stop(ctx)
                ContextCompat.startForegroundService(
                    ctx.applicationContext,
                    Intent(ctx.applicationContext, PowerConnectionService::class.java)
                )
                isRunning = true
            }
        }

        private fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, PowerConnectionService::class.java))
        }

        private fun shouldStartService(ctx: Context): Boolean {
            return !isRunning && (with(ctx.appSettingsModel) {
                isLaunchAppWhenPlugged || isExitAppWhenUnplugged || isPlaySoundWhenBatteryFull
            })
        }
    }

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private var layoutCustomNotification: RemoteViews? = null

    private var soundDisposable: Disposable? = null
    private var mediaPlayer: MediaPlayer? = null
    private var repeatTimes = 0
    private var repeatCount = 0

    private var batteryModel: BatteryModel? = null
    private var currentIconCharge: Int = 0

    private val powerConnectionReceiver by lazy { PowerConnectionReceiver() }
    private val batteryStatusReceiver by lazy {
        BatteryStatusReceiver {
            // Update notification content
            var iconCharge = 0
            layoutCustomNotification?.run {
                val imgRes = it.percentage?.let { percent ->
                    when {
                        percent == 100f -> {
                            R.drawable.battery_full
                        }
                        percent < 20f -> {
                            R.drawable.ic_battery_low
                        }
                        else -> {
                            R.drawable.ic_battery
                        }
                    }
                } ?: R.drawable.ic_battery
                iconCharge = imgRes
                setImageViewResource(R.id.img_battery, imgRes)
                setTextViewText(
                    R.id.lbl_percentage,
                    String.format(
                        getString(R.string._percentage),
                        NumberUtil.formatNumber(it.percentage)
                    )
                )
                if (it.status == 2) {
                    setTextViewText(R.id.txt_status_charge, getString(R.string.charing))
                    setTextViewText(R.id.txtTimeLeft, getString(R.string.time_charging_left) +" : "+ getTextTimeLeft(it,ctx))
                } else {
                    setTextViewText(R.id.txt_status_charge, getString(R.string.not_charing))
                    setTextViewText(R.id.txtTimeLeft, getString(R.string.time_using_left)+ " : "+getTextTimeLeft(it,ctx))
                }
                setTextViewText(
                    R.id.lbl_temperature, String.format(
                        getString(R.string._degree),
                        NumberUtil.formatNumber(it.temperatureC, 1),
                        NumberUtil.formatNumber(it.temperatureF, 1)
                    )
                )

                // Show/hide "optimize button"
                if (it.percentage == 100f) {
                    setViewVisibility(R.id.btn_optimize, View.GONE)
                } else {
                    setViewVisibility(R.id.btn_optimize, View.VISIBLE)
                }
            }

            val isChangeData = handleCheckChangeData(it, iconCharge)
            if (isChangeData) {
                notificationBuilder?.setCustomContentView(layoutCustomNotification)
                notificationManager?.notify(NOTIFICATION_ID, notificationBuilder?.build())
            }

            // Keep battery percentage in setting model
            appSettingsModel.batteryPercentage = it.percentage

            // Play battery full notification
            if (it.isCharging && appSettingsModel.isPlaySoundWhenBatteryFull && !isDontDisturb() && it.percentage == 100f) {
                playSoundProcess()
            } else {
                releaseBatteryNotificationResource()
            }
        }
    }

    private fun getTextTimeLeft(model: BatteryModel, context: Context) : String{
        if (model.isCharging) {
            val plugged = getPlugged(context)
            Log.d("HaiHT", "plugged:$plugged")
            val usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB
            val time: Int = if (usbCharge) {
                BatteryPref.initilaze(context)!!
                    .getTimeChargingUsb(context, getBatteryLevel(context))
                Log.d("HaiHT", "time USB:${BatteryPref.initilaze(context)!!
                    .getTimeChargingUsb(context, getBatteryLevel(context))}")
            } else {
                BatteryPref.initilaze(context)!!
                    .getTimeChargingAc(context, getBatteryLevel(context))
            }
             return (time / 60).toString() +"h"+(time % 60).toString()+"m"
        } else {
            val time = BatteryPref.initilaze(context)!!
                .getTimeRemainning(context, getBatteryLevel(context))
            return (time / 60).toString() +"h"+(time % 60).toString()+"m"
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        return try {
            val registerReceiver = context.applicationContext.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
            return  registerReceiver!!.getIntExtra("level", -1)
        } catch (unused: java.lang.Exception) {
            -1
        }
    }

    private fun getPlugged(context: Context): Int {
        return try {
            val registerReceiver = context.applicationContext.registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
            return  registerReceiver!!.getIntExtra("plugged", -1)
        } catch (unused: java.lang.Exception) {
            -1
        }
    }

    private fun handleCheckChangeData(newData: BatteryModel, iconCharge: Int): Boolean {
        if (batteryModel == null) {
            batteryModel = newData
            return true
        } else {
            if (batteryModel!!.status != newData.status) {
                batteryModel = newData
                return true
            }
            return if (newData.percentage != null) {
                if (newData.percentage!! >= 20f && newData.percentage!! < 100f) {
                    false
                } else if (newData.percentage!! == 100f) {
                    true
                } else {
                    if (iconCharge != currentIconCharge) {
                        currentIconCharge = iconCharge
                        true
                    } else {
                        false
                    }
                }
            } else false
        }
    }

    override fun onCreate() {
        super.onCreate()
//        detectCharging()
        registerBatteryStatusReceiver()
        registerPowerConnectionReceiver()
//        requestRestartSelfOnXiaomiDevice()
        showNotification()

        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        unregisterPowerConnectionReceiver()
        unregisterBatteryStatusReceiver()
        releaseBatteryNotificationResource()
        super.onDestroy()
    }

    private fun detectCharging() {
        // Intent to check the actions on battery
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        // isCharging if true indicates charging is ongoing and vice-versa
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING

        // Display whatever the state in the form of a Toast
        chargeBattery = if (isCharging) ({
            applicationContext.getString(R.string.charing)
        }).toString() else ({
            applicationContext.getString(R.string.not_charing)
        }).toString()
    }

    private fun showNotification() {
        if (PermissionUtil.isApi26orHigher()) {
            // Since android Oreo notification channel is needed.
            val channelId = BuildConfig.APPLICATION_ID
            if (PermissionUtil.isApi26orHigher()) {
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.x2_charger),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager?.createNotificationChannel(channel)
            }

            // Pending intent
            val pendingIntent = Intent(ctx, MainActivity::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                PendingIntent.getActivity(
                    ctx,
                    0,
                    it,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // Get the layouts to use in the custom notification
            layoutCustomNotification =
                RemoteViews(packageName, R.layout.layout_custom_notification).apply {
                    var paddingHorizontal =
                        CommonUtil.convertDpToPixel(ctx, intArrayOf(R.dimen.dimen_1x))
                    var paddingVertical =
                        CommonUtil.convertDpToPixel(ctx, intArrayOf(R.dimen.dimen_1x))
                    when {
                        isXiaomiDevice -> {
                            paddingHorizontal = 0
                            paddingVertical = 0
                        }
                        isOppoDevice -> {
                            paddingHorizontal =
                                CommonUtil.convertDpToPixel(ctx, intArrayOf(R.dimen.dimen_1x))
                            paddingVertical =
                                CommonUtil.convertDpToPixel(ctx, intArrayOf(R.dimen.dimen_05x))
                        }
                        isVivoDevice -> {
                            paddingHorizontal =
                                CommonUtil.convertDpToPixel(ctx, intArrayOf(R.dimen.dimen_2x))
                            paddingVertical = 0
                        }
                    }
                    setViewPadding(
                        R.id.frl_custom_notification,
                        paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical
                    )

                    setOnClickPendingIntent(
                        R.id.btn_optimize,
                        onNotificationButtonCLicked(
                            com.sh.entertainment.fastcharge.common.Constants.ACTION_OPTIMIZE,
                            RC_OPTIMIZE
                        )
                    )
                }

            notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.icon_app)
                .setContentTitle(chargeBattery)
                .setCustomContentView(layoutCustomNotification)
                .setAutoCancel(true)
                .setNotificationSilent()
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
            startForeground(NOTIFICATION_ID, notificationBuilder?.build())
        }
    }

    private fun registerPowerConnectionReceiver() {
        IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }.run {
            registerReceiver(powerConnectionReceiver, this)
        }
    }

    private fun unregisterPowerConnectionReceiver() {
        try {
            unregisterReceiver(powerConnectionReceiver)
        } catch (e: Exception) {
            logE(e.message.toString())
        }
    }

    private fun registerBatteryStatusReceiver() {
        BatteryStatusReceiver.register(ctx, batteryStatusReceiver)
        detectCharging()
    }

    private fun unregisterBatteryStatusReceiver() {
        try {
            unregisterReceiver(batteryStatusReceiver)
        } catch (e: Exception) {
            logE(e.message.toString())
        }
    }

    private fun onNotificationButtonCLicked(action: String, requestCode: Int): PendingIntent {
        return Intent(this, ActionHandlerActivity::class.java).apply {
            putExtra(
                com.sh.entertainment.fastcharge.common.Constants.KEY_ACTION,
                action
            )
        }.run {
            PendingIntent.getActivity(
                ctx,
                requestCode,
                this,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private fun requestRestartingService() {
        val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(ctx, PowerRestarterReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(ctx, 0, intent, 0)
        }

        alarmMgr.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 5 * 1000,
            alarmIntent
        )
    }

    private fun requestRestartSelfOnXiaomiDevice() {
        if (isXiaomiDevice && PermissionUtil.isApi26orHigher()) {
            val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(ctx, PowerRestarterReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(ctx, 0, intent, 0)
            }

            alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 30 * 60 * 1000,
                alarmIntent
            )
        }
    }

    private fun playSoundProcess() {
        if (soundDisposable == null) {
            soundDisposable = Observable.interval(0, 30, TimeUnit.SECONDS)
                .applyIOWithAndroidMainThread()
                .doOnNext {
                    startMediaPlayer()
                }
                .takeUntil {
                    it.toInt() == Int.MAX_VALUE
                }
                .subscribe()
        }
    }

    private fun startMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(ctx, R.raw.notification_battery_full)
            mediaPlayer?.setOnCompletionListener {
                repeatCount++
                if (repeatCount < 2 && repeatTimes < 3) {
                    it.seekTo(0)
                    it.start()
                }
            }
        }
        mediaPlayer?.apply {
            start()
            if (repeatTimes < 2) {
                repeatCount = 0
                repeatTimes++
            }
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            stop()
            reset()
            release()
            mediaPlayer = null
        }
    }

    private fun releaseBatteryNotificationResource() {
        releasePlayer()
        soundDisposable?.dispose()
        soundDisposable = null
        repeatTimes = 0
        repeatCount = 0
    }

    private fun isDontDisturb(): Boolean {
        var dontDisturb = false
        with(appSettingsModel) {
            if (dontPlaySoundWhile) {
                val cal = Calendar.getInstance()
                val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                val currentMin = cal.get(Calendar.MINUTE)
                val currentTime = "${NumberUtil.getTwoDigitsNumber(currentHour)}${
                    NumberUtil.getTwoDigitsNumber(currentMin)
                }".toInt()
                val dontPlayFromTime = "${NumberUtil.getTwoDigitsNumber(dontPlaySoundFromHour)}${
                    NumberUtil.getTwoDigitsNumber(dontPlaySoundFromMin)
                }".toInt()
                val dontPlayToTime = "${NumberUtil.getTwoDigitsNumber(dontPlaySoundToHour)}${
                    NumberUtil.getTwoDigitsNumber(dontPlaySoundToMin)
                }".toInt()

                dontDisturb = when {
                    dontPlayFromTime > dontPlayToTime -> {
                        currentTime >= dontPlayFromTime || currentTime <= dontPlayToTime
                    }
                    else -> {
                        currentTime in dontPlayFromTime..dontPlayToTime
                    }
                }
            }
        }

        return dontDisturb
    }
}