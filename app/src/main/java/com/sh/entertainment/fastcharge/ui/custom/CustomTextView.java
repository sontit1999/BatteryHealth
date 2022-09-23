package com.sh.entertainment.fastcharge.ui.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class CustomTextView extends AppCompatTextView {
    public CustomTextView(@NonNull Context context) {
        super(context);
        applyFont();
    }

    public CustomTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyFont();
    }

    public CustomTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyFont();
    }

    public void applyFont() {
        Typeface tf = Typeface.createFromAsset(this.getContext().getAssets(), "quicksand_light.ttf");
        setTypeface(tf);
    }
}
