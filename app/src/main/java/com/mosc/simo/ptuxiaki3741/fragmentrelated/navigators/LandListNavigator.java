package com.mosc.simo.ptuxiaki3741.fragmentrelated.navigators;

import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.LandListFragmentDirections;
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
        NavDirections action = LandListFragmentDirections.createLand(new Land());
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
            if(land.getLandData() != null){
                return LandListFragmentDirections.editLand(land);
            }else{
                return LandListFragmentDirections.createLand(new Land());
            }
        }else{
            return LandListFragmentDirections.createLand(new Land());
        }
    }
}
