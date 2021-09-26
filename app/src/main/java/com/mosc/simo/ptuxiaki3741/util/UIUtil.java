package com.mosc.simo.ptuxiaki3741.util;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mosc.simo.ptuxiaki3741.R;

public final class UIUtil {
    private UIUtil(){}

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
    public static NavController getNavController(Fragment fragment, int fragment_id){
        NavController navController = NavHostFragment.findNavController(fragment);
        if(navController.getCurrentDestination() == null)
            return navController;
        else if(navController.getCurrentDestination().getId() == fragment_id)
            return navController;
        return null;
    }
    public static boolean isValidMobileNo(String phoneNumber){
        String regexStr1 = "^[0-9]{10}$";
        String regexStr2 = "^\\+[0-9]{12}$";
        return phoneNumber.matches(regexStr1) || phoneNumber.matches(regexStr2) ;
    }
    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(
                context.getString(R.string.clipboard_message),
                text
        );
        clipboard.setPrimaryClip(clip);
    }
}
