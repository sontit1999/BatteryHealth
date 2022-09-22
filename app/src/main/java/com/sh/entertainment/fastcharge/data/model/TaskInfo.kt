package com.sh.entertainment.fastcharge.data.model

import android.annotation.SuppressLint
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.widget.CheckBox

class TaskInfo : Parcelable {
    var appinfo: ApplicationInfo = ApplicationInfo()
    var isChceked = false
    var chkTask: CheckBox? = null
    var mem: Long = 0
    var pid = 0
    var pkgInfo: PackagesInfo? = null
    var pm: PackageManager? = null
    var runinfo: RunningAppProcessInfo? = null
    var title: String? = null

    override fun describeContents(): Int {
        return 0
    }

    constructor(context: Context, applicationInfo: ApplicationInfo) {
        pkgInfo = null
        runinfo = null
        title = null
        appinfo = applicationInfo
        pm = context.applicationContext.packageManager
    }

    protected constructor(parcel: Parcel) {
        appinfo = parcel.readParcelable<Parcelable>(ApplicationInfo::class.java.classLoader) as ApplicationInfo
        mem = parcel.readLong()
        runinfo = parcel.readParcelable<Parcelable>(RunningAppProcessInfo::class.java.classLoader) as RunningAppProcessInfo?
        title = parcel.readString().toString()
        isChceked = parcel.readByte().toInt() != 0
        pid = parcel.readInt()
    }


    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeParcelable(appinfo, i)
        parcel.writeLong(mem)
        parcel.writeParcelable(runinfo, i)
        parcel.writeString(title)
        parcel.writeByte(if (isChceked) 1.toByte() else 0)
        parcel.writeInt(pid)
    }

    companion object {
        @SuppressLint("ParcelCreator")
        val CREATOR: Creator<TaskInfo> = object : Creator<TaskInfo> {
            override fun createFromParcel(parcel: Parcel): TaskInfo {
                return TaskInfo(parcel)
            }

            override fun newArray(i: Int): Array<TaskInfo?> {
                return arrayOfNulls(i)
            }
        }
    }
}