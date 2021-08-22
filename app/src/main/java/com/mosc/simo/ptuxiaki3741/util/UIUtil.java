package com.mosc.simo.ptuxiaki3741.util;

import android.app.Activity;
import android.content.Context;

import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
    public static boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
