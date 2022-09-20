package com.procharger.fastprocharrging.quickcharge.ui.home

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.extension.appSettingsModel
import com.procharger.fastprocharrging.quickcharge.common.extension.applyIOWithAndroidMainThread
import com.procharger.fastprocharrging.quickcharge.common.extension.ctx
import com.procharger.fastprocharrging.quickcharge.common.util.NumberUtil
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

class BatteryStatusService : Service() {

    private var soundDisposable: Disposable? = null
    private var mediaPlayer: MediaPlayer? = null
    private var repeatTimes = 0
    private var repeatCount = 0

    private val batteryStatusReceiver by lazy {
        BatteryStatusReceiver() {
            if (appSettingsModel.isPlaySoundWhenBatteryFull && !isDontDisturb() && it.percentage == 100f) {
                playSoundProcess()
            }
        }
    }

    companion object {
        fun start(ctx: Context) {
            stop(ctx)
            ctx.startService(Intent(ctx, BatteryStatusService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, BatteryStatusService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        BatteryStatusReceiver.register(ctx, batteryStatusReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return startId
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        BatteryStatusReceiver.unregister(ctx, batteryStatusReceiver)
        releasePlayer()
        soundDisposable?.dispose()
        super.onDestroy()
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