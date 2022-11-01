package com.entertainment.basemvvmproject.utils

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager


object ViewUtils {
    //check double click
    @kotlin.jvm.JvmStatic
    fun runLayoutAnimation(recyclerView: RecyclerView, @AnimRes resId: Int) {
        val context = recyclerView.context
        val controller =
            AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = controller
        recyclerView.scheduleLayoutAnimation()
    }
}

fun String.toast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, this, duration).show()
}

fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}


fun ImageView.tint(@ColorRes colorId: Int) {
    setColorFilter(context.getColorCompat(colorId))
}

fun EditText.onTextChange(content: (Editable?) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //do nothing
        }

        override fun afterTextChanged(s: Editable?) {
            content(s)
        }
    })
}

fun ViewPager.onPageSelected(params: (Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            //do nothing
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            //do nothing
        }

        override fun onPageSelected(position: Int) {
            params(position)
        }

    })
}

fun View.setOnClickAction(listener: View.OnClickListener) {

}

/*fun TextView.setTextAsync(data: String) {
    TextViewCompat.setPrecomputedText(
        this,
        PrecomputedTextCompat.create(data, TextViewCompat.getTextMetricsParams(this))
    )
}*/

fun Activity.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toastMessage(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}