package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.navigators;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.fragments.LandListFragmentDirections;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;

public class LandListNavigator {
    private final NavController navController;

    public LandListNavigator(NavController navController){
        this.navController = navController;
    }


    private void navigate(NavDirections action){
        if(action != null){
            if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.LandListFragment)
                navController.navigate(action);
        }
    }
    private NavDirections getEditLandAction(Land land){
        if(land != null){
            if(land.getData() != null){
                return LandListFragmentDirections.toLandMap(land);
            }else{
                return LandListFragmentDirections.toLandInfo(new Land());
            }
        }else{
            return LandListFragmentDirections.toLandInfo(new Land());
        }
    }

    public void toEditLand(@Nullable Activity activity, Land land){
        NavDirections action = getEditLandAction(land);
        if(activity != null){
            activity.runOnUiThread(()->navigate(action));
        }
    }
    public void toLandInfo(@Nullable Activity activity){
        NavDirections action = LandListFragmentDirections.toLandInfo(new Land());
        if(activity != null){
            activity.runOnUiThread(()->navigate(action));
        }
    }
    public void toMenu(@Nullable Activity activity) {
        NavDirections action = LandListFragmentDirections.toMenu();
        if(activity != null){
            activity.runOnUiThread(()->navigate(action));
        }
    }
}
