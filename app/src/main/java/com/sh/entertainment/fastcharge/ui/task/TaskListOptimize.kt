package com.sh.entertainment.fastcharge.ui.task

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.AsyncTask
import android.os.Build
import android.widget.TextView
import com.sh.entertainment.fastcharge.data.model.TaskInfo

class TaskListOptimize(
    context: Context,
    textView: TextView,
    onTaskListListener: OnTaskListListener
) :
    AsyncTask<Void, TaskInfo, ArrayList<TaskInfo>?>() {
    var mActivityManager: ActivityManager
    private val mContext: Context = context
    private val mOnTaskListListener: OnTaskListListener = onTaskListListener
    private val mPackageManager: PackageManager? = context.packageManager
    private val mTotal: Long = 0
    var titleApp: TextView

    interface OnTaskListListener {
        fun OnResult()
    }

    /* access modifiers changed from: protected */
    public override fun onPreExecute() {}

    /* access modifiers changed from: protected */

    @SuppressLint("WrongConstant")
    override fun doInBackground(vararg voidArr: Void): ArrayList<TaskInfo>? {
        val activityManager = mContext.getSystemService("activity") as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        return when {
            Build.VERSION.SDK_INT <= 21 -> {
                for (runningAppProcessInfo in runningAppProcesses) {
                    try {
                        if (mPackageManager == null) {
                            return null
                        }
                        val str = runningAppProcessInfo.processName
                        val applicationInfo = mPackageManager.getApplicationInfo(str, 0)
                        if (!str.contains(mContext.packageName)) {
                            val taskInfo = TaskInfo(mContext, applicationInfo)
                            mActivityManager.killBackgroundProcesses(taskInfo.appinfo.packageName)
                            publishProgress(taskInfo)
                            try {
                                Thread.sleep(150)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    } catch (unused: Exception) {
                    }
                }
                null
            }
            Build.VERSION.SDK_INT < 26 -> {
                for (next in activityManager.getRunningServices(Int.MAX_VALUE)) {
                    try {
                        if (mPackageManager == null) {
                            return null
                        }
                        val packageInfo =
                            mPackageManager.getPackageInfo(
                                next.service.packageName,
                                PackageManager.GET_ACTIVITIES
                            )
                        if (packageInfo != null) {
                            val applicationInfo2 =
                                mPackageManager.getApplicationInfo(packageInfo.packageName, 0)
                            if (!packageInfo.packageName.contains(mContext.packageName)) {
                                val taskInfo2 = TaskInfo(mContext, applicationInfo2)
                                mActivityManager.killBackgroundProcesses(taskInfo2.appinfo.packageName)
                                publishProgress(taskInfo2)
                                try {
                                    Thread.sleep(150)
                                } catch (e2: InterruptedException) {
                                    e2.printStackTrace()
                                }
                            }
                        }
                    } catch (unused2: Exception) {
                    }
                }
                null
            }
            else -> {
                for (next2 in mContext.packageManager.getInstalledPackages(21375)) {
                    val packageManager = mPackageManager ?: return null
                    try {
                        val applicationInfo3 =
                            packageManager.getApplicationInfo(next2.packageName, 0)
                        val serviceInfoArr: Array<ServiceInfo> = next2.services
                        if (serviceInfoArr != null && serviceInfoArr.isNotEmpty()
                            && !next2.packageName.contains(mContext.packageName)
                        ) {
                            val taskInfo3 = TaskInfo(mContext, applicationInfo3)
                            mActivityManager.killBackgroundProcesses(taskInfo3.appinfo.packageName)
                            publishProgress(taskInfo3)
                            try {
                                Thread.sleep(150)
                            } catch (e3: InterruptedException) {
                                e3.printStackTrace()
                            }
                        }
                        val permissionInfoArr = next2.permissions
                    } catch (e4: PackageManager.NameNotFoundException) {
                        e4.printStackTrace()
                    }
                }
                null
            }
        }
    }

    /* access modifiers changed from: protected */
    public override fun onProgressUpdate(vararg taskInfoArr: TaskInfo) {
        super.onProgressUpdate(*taskInfoArr)
        getImageApp(taskInfoArr[0])
    }

    /* access modifiers changed from: protected */
    public override fun onPostExecute(arrayList: ArrayList<TaskInfo>?) {
        val onTaskListListener = mOnTaskListListener
        onTaskListListener.OnResult()
    }

    @SuppressLint("SetTextI18n")
    fun getImageApp(taskInfo: TaskInfo) {
        val textView = titleApp
        textView.text = "Scanning" + ": " + taskInfo.title
    }

    init {
        titleApp = textView
        mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

}