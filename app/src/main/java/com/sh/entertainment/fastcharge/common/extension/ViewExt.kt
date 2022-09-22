package com.sh.entertainment.fastcharge.common.extension

import android.content.Context
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sh.entertainment.fastcharge.common.util.SafeOnClickListener

inline val View.ctx: Context
    get() = context

var TextView.textColor: Int
    get() = currentTextColor
    set(value) = setTextColor(ContextCompat.getColor(ctx, value))

fun View.gone() {
    visibility = GONE
}

fun View.visible() {
    visibility = VISIBLE
}

fun View.invisible() {
    visibility = INVISIBLE
}

fun ViewGroup.setAnimation(visibility: Int, animation: Int) {
    val anim = AnimationUtils.loadAnimation(ctx, animation)
    val animController = LayoutAnimationController(anim)

    this.visibility = visibility
    layoutAnimation = animController
    startAnimation(anim)
}

fun View.setOnSafeClickListener(safeTime: Long = 300, clickListener: (View?) -> Unit) {
    setOnClickListener(SafeOnClickListener.newInstance(safeTime) {
        clickListener(it)
    })
}