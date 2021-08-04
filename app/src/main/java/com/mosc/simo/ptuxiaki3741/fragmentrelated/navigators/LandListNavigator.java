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
    public void toLandInfo(Activity activity){
        NavDirections action = LandListFragmentDirections.toLandInfo(new Land());
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
                return LandListFragmentDirections.toLandMap(land);
            }else{
                return LandListFragmentDirections.toLandInfo(new Land());
            }
        }else{
            return LandListFragmentDirections.toLandInfo(new Land());
        }
    }

    public void toMenu(Activity activity) {
        NavDirections action = (NavDirections) LandListFragmentDirections.toMenu();
        activity.runOnUiThread(()->navigate(action));
    }
}
