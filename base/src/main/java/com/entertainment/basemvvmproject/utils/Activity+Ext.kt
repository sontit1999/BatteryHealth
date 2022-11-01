package com.entertainment.basemvvmproject.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


fun Context.checkPermissions(permissions: Array<String>): Array<String> {
    val array = mutableListOf<String>()
    for (p in permissions) {
        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission(p, packageName))
            array.add(p)
    }
    return array.toTypedArray()
}

fun AppCompatActivity.requestPermissions(code: Int, permissions: Array<String>): Boolean {
    val ls = checkPermissions(permissions)
    return if (ls.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, ls, code)
        false
    } else {
        true
    }
}

fun Fragment.requestPermissions(code: Int, permissions: Array<String>): Boolean {
    val con = context ?: return false
    val ls = con.checkPermissions(permissions)
    return if (ls.isNotEmpty()) {
        requestPermissions(ls, code)
        false
    } else {
        true
    }
}

fun Fragment.requestPermissionStorage(code: Int): Boolean {
    val con = context ?: return false
    val ls = con.checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    return if (ls.isNotEmpty()) {
        requestPermissions(ls, code)
        false
    } else {
        true
    }
}

fun AppCompatActivity.requestPermissionStorage(code: Int): Boolean {
    val ls = checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    return if (ls.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, ls, code)
        false
    } else {
        true
    }
}

val Context.checkPermissionStorage : Boolean get() = checkPermissions(
    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)).isEmpty()

fun AppCompatActivity.openNetworkSettings() : Boolean {
    if (!openIntent(Intent(Settings.ACTION_WIRELESS_SETTINGS))) {
        return openIntent(Intent(Settings.ACTION_SETTINGS))
    }
    return true
}

fun AppCompatActivity.openIntent(intent: Intent) : Boolean {
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
    } catch (e: Exception) {

    }
    return false
}