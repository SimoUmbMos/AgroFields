package com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.model.User;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.LandListFragmentDirections;

import java.util.ArrayList;
import java.util.List;

public class LandListNavigator {
    private final NavController navController;
    private final User user;

    public LandListNavigator(NavController navController, User user){
        this.navController = navController;
        this.user = user;
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
                return LandListFragmentDirections.editLand(land,landPointArray,false);
            }else{
                return LandListFragmentDirections.editLand(land,null,false);
            }
        }else{
            return null;
        }
    }
    public void toEditLand(Land land){
        NavDirections action = getEditLandAction(land, new ArrayList<>());
        if(action != null){
            navigate(action);
        }else{
            toCreateLand();
        }
    }

    public void toCreateLand(){
        Land land = new Land(-1,-1,"");
        NavDirections action = LandListFragmentDirections.createLand(land,user);
        navigate(action);
    }
    public void toEditLandInfo(Land land){
        NavDirections action = LandListFragmentDirections.createLand(land,user);
        navigate(action);
    }

    public void toLandExport(List<Land> lands){

    }
}
