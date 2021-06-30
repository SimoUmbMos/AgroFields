package com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers;

import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.LandListFragmentDirections;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;

import java.util.List;

public class LandListNavigator {
    private final NavController navController;

    public LandListNavigator(NavController navController){
        this.navController = navController;
    }

    private void navigate(NavDirections action){
        if(action != null){
            navController.navigate(action);
        }
    }

    private NavDirections getEditLandAction(Land land, List<LandPoint> landPoints){
        if(landPoints != null){
            LandPoint[] landPointArray = new LandPoint[landPoints.size()];
            for(int i = 0; i < landPoints.size(); i++) landPointArray[i] = landPoints.get(i);
            return LandListFragmentDirections.editLand(land,landPointArray);
        }else{
            return LandListFragmentDirections.editLand(land,null);
        }
    }
    public void toEditLand(Activity activity, LandViewModel vmLands, int position){
        List<Land> lands = vmLands.getLands().getValue();
        if(lands != null){
            if(lands.size() > position && position > -1){
                Land land = vmLands.getLands().getValue().get(position);
                List<LandPoint> landPoints = vmLands.getLandPoints(land.getId());
                NavDirections action = getEditLandAction(land,landPoints);
                activity.runOnUiThread(()->navigate(action));
            }
        }
    }

    public void toCreateLand(Activity activity){
        Land land = new Land(-1,-1,"");
        NavDirections action = LandListFragmentDirections.createLand(land,null);
        activity.runOnUiThread(()->navigate(action));
    }

    public void toLandExport(List<Land> lands){

    }
}
