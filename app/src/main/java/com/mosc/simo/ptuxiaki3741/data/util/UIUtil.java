package com.mosc.simo.ptuxiaki3741.data.util;

import android.content.Context;

import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mosc.simo.ptuxiaki3741.data.models.ColorData;

public final class UIUtil {
    private UIUtil(){}

    public static NavController getNavController(Fragment fragment, int fragment_id){
        if(fragment == null) return null;
        NavController navController = NavHostFragment.findNavController(fragment);
        if(navController.getCurrentDestination() == null)
            return navController;
        else if(navController.getCurrentDestination().getId() == fragment_id)
            return navController;
        return null;
    }

    public static boolean showBlackText(final ColorData color) {
        if(color == null) return true;
        double[] cp = new double[3];
        cp[0] = color.getRed() / 255.0;
        cp[1] = color.getGreen() / 255.0;
        cp[2] = color.getBlue() / 255.0;
        for(int i = 0; i < cp.length; i++){
            if(cp[i] <= 0.03928){
                cp[i] = cp[i]/12.92;
            }else{
                cp[i] = Math.pow( (cp[i]+0.055) / 1.055 , 2.4 );
            }
        }
        double luminance = 0.2126 * cp[0] + 0.7152 * cp[1] + 0.0722 * cp[2];
        return luminance > 0.179;
    }

    public static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
