package com.sh.entertainment.fastcharge.common.util

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.sh.entertainment.fastcharge.BuildConfig
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.ui.main.MainActivity
import java.util.*

object NotificationCenter {

    const val ACTION_NOTIFICATION = BuildConfig.APPLICATION_ID + ".notification"
    const val CHANNEL_ID = "Fast Charging Pro"
    const val EXTRA_TAG = "NOTIFICATION"
    const val MESSAGE = "MESSAGE"
    const val TITLE = "TITLE"
    const val ID = "ID"
    const val DATA = "DATA"
    const val ACTION = "ACTION"

    @JvmStatic
    fun push(bundle: Bundle, bigPicture: Bitmap? = null, image: String? = null): Boolean {

        val app = MyApplication.instance
        val con = app.applicationContext ?: return false

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour <= 6) return false
        val notificationId = System.currentTimeMillis().toInt()
        bundle.putString(ID, notificationId.toString() + "")

        val title = bundle.getString(TITLE)
        val message = bundle.getString(MESSAGE)
        val intent = Intent()
        intent.action = ACTION_NOTIFICATION
        intent.setClass(con, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(EXTRA_TAG, bundle)
        val pendingIntent = PendingIntent.getActivity(
            con,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(con, CHANNEL_ID)
            .setSmallIcon(R.mipmap.icon_app)
            .setTicker(title)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(sound)
            .setContentIntent(pendingIntent)

        builder.setLargeIcon(BitmapFactory.decodeResource(con.resources, R.mipmap.icon_app))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        val manager = con.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(manager, CHANNEL_ID)
        manager.notify(notificationId, builder.build())
        return true
    }


    fun createNotificationChannel(manager: NotificationManager, channel: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fast Charging Pro"
            val description = "Notification daly remind user open app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channel, name, importance)
            notificationChannel.description = description
            manager.createNotificationChannel(notificationChannel)
        }
    }


    @JvmStatic
    fun isApplicationInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = activityManager.runningAppProcesses ?: return false
        for (processInfo in processes) {
            if (processInfo.processName == context.packageName && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }

}