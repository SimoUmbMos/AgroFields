package com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers;

import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.database.repositorys.LandRepository;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.LandListFragmentDirections;

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
        if(land != null){
            if(landPoints != null){
                LandPoint[] landPointArray = new LandPoint[landPoints.size()];
                for(int i = 0; i < landPoints.size(); i++) landPointArray[i] = landPoints.get(i);
                return LandListFragmentDirections.editLand(land,landPointArray);
            }else{
                return LandListFragmentDirections.editLand(land,null);
            }
        }else{
            return null;
        }
    }
    public void toEditLand(Activity activity, Land land, LandRepository landRepository){
        List<LandPoint> landPoints = landRepository.getLandPoints(land.getId());
        NavDirections action = getEditLandAction(land,landPoints);
        activity.runOnUiThread(()->navigate(action));
    }

    public void toCreateLand(Activity activity){
        Land land = new Land(-1,-1,"");
        NavDirections action = LandListFragmentDirections.createLand(land,null);
        activity.runOnUiThread(()->navigate(action));
    }

    public void toLandExport(List<Land> lands){

    }
}
