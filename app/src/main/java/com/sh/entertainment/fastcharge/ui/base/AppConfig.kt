package com.sh.entertainment.fastcharge.ui.base

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object AppConfig {

    lateinit var displayMetrics: DisplayMetrics

    fun setUp(context: Context){
       displayMetrics =  getScreen(context)
    }

    private fun getScreen(context: Context): DisplayMetrics {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(dm)
        return dm
    }
}