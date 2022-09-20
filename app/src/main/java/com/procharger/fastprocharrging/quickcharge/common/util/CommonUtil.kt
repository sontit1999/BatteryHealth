package com.procharger.fastprocharrging.quickcharge.common.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.procharger.fastprocharrging.quickcharge.BuildConfig
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.extension.sharedPref
import com.procharger.fastprocharrging.quickcharge.common.extension.toast
import com.procharger.fastprocharrging.quickcharge.common.util.SharedPreferencesUtil.get
import com.procharger.fastprocharrging.quickcharge.common.util.SharedPreferencesUtil.set
import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import java.util.*

object CommonUtil {

    private const val APP_SETTINGS_MODEL = "app_settings_model"
    private const val IS_AUTO = "app_settings_is_auto"

    fun showKeyboard(ctx: Context, view: View) {
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    fun closeKeyboard(activity: AppCompatActivity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun closeKeyboardWhileClickOutSide(activity: AppCompatActivity, view: View?) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view?.setOnTouchListener { _, _ ->
                closeKeyboard(activity)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                closeKeyboardWhileClickOutSide(activity, innerView)
            }
        }
    }

    fun getHeightOfStatusBar(activity: AppCompatActivity): Int {
        val resId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            activity.resources.getDimensionPixelSize(resId)
        } else {
            0
        }
    }

    fun getHeightOfNavigationBar(activity: AppCompatActivity): Int {
        val resId = activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) {
            activity.resources.getDimensionPixelSize(resId)
        } else {
            0
        }
    }

    fun getRealScreenSizeAsPixels(activity: AppCompatActivity): Point {
        val display = if (PermissionUtil.isApi30orHigher()) {
            activity.display
        } else {
            activity.windowManager.defaultDisplay
        }

        val outPoint = Point()
        display?.getRealSize(outPoint)
        return outPoint
    }

    fun convertDpToPixel(ctx: Context?, dimensionIds: IntArray): Int {
        var result = 0
        ctx?.run {
            for (id in dimensionIds) {
                result += resources.getDimension(id).toInt()
            }
        }

        return result
    }

    /**
     * If you want to call this method on api 23 or higher then you have to check permission in runtime
     * (use PermissionUtil class for reference)
     *
     * @param act
     * @param phoneNumber
     */
    fun call(act: AppCompatActivity, phoneNumber: String) {
        val number = "tel:$phoneNumber"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse(number))
        if (callIntent.resolveActivity(act.packageManager) != null) {
            act.startActivity(callIntent)
        } else {
            act.toast("No call service found")
        }
    }

    fun sendEmail(
        ctx: Context?,
        email: String,
        subject: String,
        content: String,
        bccEmail: String? = null
    ) {
        ctx?.also {
            if (email.isNotEmpty() && email != "null") {
                Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    bccEmail?.run {
                        putExtra(Intent.EXTRA_BCC, arrayOf(bccEmail))
                    }
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, content)
                }.run {
                    try {
                        ctx.startActivity(
                            Intent.createChooser(
                                this,
                                ctx.getString(R.string.alert_button_rate_app)
                            )
                        )
                    } catch (ex: ActivityNotFoundException) {
                        ctx.toast("No email client installed")
                    }
                }
            } else {
                ctx.toast("Invalid email")
            }
        }
    }

    fun shareText(ctx: Context?, body: String) {
        ctx?.also {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, ctx?.getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, body)
            }.run {
                ctx.startActivity(Intent.createChooser(this, ctx.getString(R.string.x2_charger)))
            }
        }
    }

    fun openBrowser(ctx: Context, url: String) {
        val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (callIntent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(callIntent)
        } else {
            ctx.toast("No browser found")
        }
    }

    fun setDefaultLanguage(ctx: Context) {
        with(ctx.resources) {
            configuration.setLocale(Locale.getDefault())
            ctx.createConfigurationContext(configuration)
        }
    }

    fun openAppInPlayStore(ctx: Context) {
        val uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        if (intent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(intent)
        } else {
            openBrowser(ctx, com.procharger.fastprocharrging.quickcharge.common.Constants.LINK_APP_ON_STORE)
        }
    }

    fun saveAppSettingsModel(ctx: Context?, model: AppSettingsModel) {
        ctx?.run {
            sharedPref[APP_SETTINGS_MODEL] = model.toJson()
        }
    }

    fun saveIsAuto(context: Context?,value: Boolean){
        context?.run {
            sharedPref[IS_AUTO] = value
        }
    }

    fun getIsAuto(context: Context): Boolean{
        return  context.sharedPref[IS_AUTO]?: false
    }

    fun getAppSettingsModel(ctx: Context): AppSettingsModel {
        val json: String? = ctx.sharedPref[APP_SETTINGS_MODEL]
        return json?.run { Gson().fromJson(json, AppSettingsModel::class.java) }
            ?: AppSettingsModel()
    }
}