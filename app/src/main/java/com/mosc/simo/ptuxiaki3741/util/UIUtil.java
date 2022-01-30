package com.mosc.simo.ptuxiaki3741.util;

import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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
}
