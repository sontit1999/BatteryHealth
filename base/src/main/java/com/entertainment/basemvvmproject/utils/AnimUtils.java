package com.entertainment.basemvvmproject.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by LamTH on 9/26/2019.
 */
public class AnimUtils {


    public static void setAnimationRecycleView(final RecyclerView recyclerView, int resourceId) {
        //set animation for recyclerView
        final Context context = recyclerView.getContext();

        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, resourceId);

        recyclerView.setLayoutAnimation(controller);
        if (recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public static void setAnimationView(View view, int resourceId) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), resourceId));
    }

    public static void setAnimInfinityImage(final ImageView imageView1, final ImageView imageView2, long duration) {
        final ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            final float progress = (float) animation.getAnimatedValue();
            final float width = imageView1.getWidth();
            final float translationX = width * progress;
            imageView1.setTranslationX(translationX);
            imageView2.setTranslationX(translationX - width);
        });
        animator.start();
    }

    public static void scaleView(View v, float endX, float endY) {
        Animation anim = new ScaleAnimation(
                1f, endX, // Start and end values for the X axis scaling
                1f, endY, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(500);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        v.startAnimation(anim);
    }
}
