package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;

import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.mosc.simo.ptuxiaki3741.R;

public class UIUtil {
    public static float floatToDPMetric(float number, Context context){
        if(context != null)
            return number * context.getResources().getDisplayMetrics().density;
        return number;
    }
    public static int getColorOnPrimaryFromTheme(@NonNull Context context){
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
            return typedValue.data;
    }
}
