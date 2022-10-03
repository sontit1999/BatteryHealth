package com.sh.entertainment.fastcharge.worker

import android.content.Context
import android.os.Bundle
import androidx.work.*
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.Constants
import com.sh.entertainment.fastcharge.common.MyApplication
import com.sh.entertainment.fastcharge.common.util.NotificationCenter
import com.sh.entertainment.fastcharge.common.util.SharedPreferencesUtil
import com.sh.entertainment.fastcharge.common.util.SharedPreferencesUtil.get
import com.sh.entertainment.fastcharge.common.util.SharedPreferencesUtil.set
import java.util.concurrent.TimeUnit


class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    //4 types of notification, each has 3 types of content
    private val listDayOneNotificationContent = arrayOf(
        Pair(R.string.dayOneNotificationTitle01, R.string.dayOneNotificationMessage01),
        Pair(R.string.dayOneNotificationTitle02, R.string.dayOneNotificationMessage02),
        Pair(R.string.dayOneNotificationTitle03, R.string.dayOneNotificationMessage03)
    )

    override fun doWork(): Result {
        // do anything
        pushNotification()
        reschedule()
        return Result.success()
    }

    private fun pushNotification() {
        try {
            val currentPosition =
                SharedPreferencesUtil.customPrefs(applicationContext)[Constants.KEY_POSITION_NOTIFICATION, 0]
                    ?: 0
            val data = listDayOneNotificationContent[currentPosition]
            val bundle = Bundle()
            bundle.putString(NotificationCenter.TITLE, applicationContext.getString(data.first))
            bundle.putString(
                NotificationCenter.MESSAGE, String.format(
                    applicationContext.getString(data.second), applicationContext.getString(
                        R.string.app_name
                    )
                )
            )
            bundle.putString(NotificationCenter.ACTION, TAGS)
            bundle.putString(NotificationCenter.DATA, currentPosition.toString())
            NotificationCenter.push(bundle)
        } catch (e: Exception) {

        }
    }

    private fun reschedule() {
        try {
            val index =
                SharedPreferencesUtil.customPrefs(applicationContext)[Constants.KEY_POSITION_NOTIFICATION, 0]
                    ?: 0
            if (index < 2) SharedPreferencesUtil.customPrefs(applicationContext)[Constants.KEY_POSITION_NOTIFICATION] =
                index + 1 else SharedPreferencesUtil.customPrefs(applicationContext)[Constants.KEY_POSITION_NOTIFICATION] =
                0
            schedule()
        } catch (e: Exception) {
        }
    }

    companion object {
        const val TAGS = "NotificationWorker"

        fun schedule() {
            val delta = 4 * 60 * 60 * 1000L
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work: OneTimeWorkRequest =
                OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                    .setInitialDelay(delta, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(MyApplication.instance)
                .enqueueUniqueWork(TAGS, ExistingWorkPolicy.REPLACE, work)
        }

        fun cancel() {
            WorkManager.getInstance(MyApplication.instance).cancelUniqueWork(TAGS)
        }
    }
}