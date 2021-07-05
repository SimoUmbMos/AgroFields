package com.mosc.simo.ptuxiaki3741.navigators;

import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.fragments.LandListFragmentDirections;
import com.mosc.simo.ptuxiaki3741.models.Land;

import java.util.List;

public class LandListNavigator {
    private final NavController navController;

    public LandListNavigator(NavController navController){
        this.navController = navController;
    }

    public void toEditLand(Activity activity, Land land){
        NavDirections action = getEditLandAction(land);
        activity.runOnUiThread(()->navigate(action));
    }
    public void toCreateLand(Activity activity){
        Land land = new Land();
        NavDirections action = LandListFragmentDirections.createLand(land);
        activity.runOnUiThread(()->navigate(action));
    }
    public void toLandExport(Activity activity, List<Land> lands){

    }

    private void navigate(NavDirections action){
        if(action != null){
            navController.navigate(action);
        }
    }
    private NavDirections getEditLandAction(Land land){
        if(land != null){
            return LandListFragmentDirections.editLand(land);
        }else{
            return LandListFragmentDirections.createLand(new Land());
        }
    }
}
