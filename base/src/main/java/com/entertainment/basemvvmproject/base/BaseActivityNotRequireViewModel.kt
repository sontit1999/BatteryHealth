package com.example.demoandroidrikkei.base.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.entertainment.basemvvmproject.base.LoadingDialog
import timber.log.Timber
import java.lang.ref.WeakReference

const val DURATION_TIME_CLICKABLE = 500

abstract class BaseActivityNotRequireViewModel<BD : ViewDataBinding> : AppCompatActivity() {

    private var _binding: BD? = null
    protected val binding: BD get() = _binding!!

    private var lastTimeClick: Long = 0

    @get: LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(WeakReference(this).get()!!, layoutId)
        _binding?.lifecycleOwner = this

    }

    override fun onDestroy() {
        _binding?.unbind()
        _binding = null
        super.onDestroy()
    }

    fun showLoading(isShow: Boolean, fragmentManager: FragmentManager? = null) {
        if (isShow) {
            LoadingDialog.show(fragmentManager ?: supportFragmentManager)
        } else {
            LoadingDialog.hidden(fragmentManager ?: supportFragmentManager)
        }
    }

    //click able
    val isDoubleClick: Boolean
        get() {
            val timeNow = SystemClock.elapsedRealtime()
            if (timeNow - lastTimeClick >= DURATION_TIME_CLICKABLE) {
                //click able
                lastTimeClick = timeNow
                return false
            }
            return true
        }


    /**
     * Close SoftKeyboard when user click out of EditText
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Timber.d("onBackPressed in activity")
    }
}