package com.mosc.simo.ptuxiaki3741.fragmentrelated.navigators;

import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.LandListFragmentDirections;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;

import java.util.List;

public class LandListNavigator {
    public static final String TAG = "LandListNavigator";
    private final NavController navController;

    public LandListNavigator(NavController navController){
        this.navController = navController;
    }

    public void toEditLand(Activity activity, Land land){
        NavDirections action = getEditLandAction(land);
        activity.runOnUiThread(()->navigate(action));
    }
    public void toCreateLand(Activity activity){
        NavDirections action = (NavDirections) LandListFragmentDirections.createLand(new Land());
        activity.runOnUiThread(()->navigate(action));
    }
    public void toLandExport(Activity activity, List<Land> lands){

    }

    private void navigate(NavDirections action){
        if(action != null){
            if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.landListFragment)
                navController.navigate(action);
        }
    }
    private NavDirections getEditLandAction(Land land){
        if(land != null){
            if(land.getData() != null){
                return (NavDirections) LandListFragmentDirections.editLand(land);
            }else{
                return (NavDirections) LandListFragmentDirections.createLand(new Land());
            }
        }else{
            return (NavDirections) LandListFragmentDirections.createLand(new Land());
        }
    }

    public void toLogin(Activity activity) {
        NavDirections action = (NavDirections) LandListFragmentDirections.toLogin();
        activity.runOnUiThread(()->navigate(action));
    }
}
