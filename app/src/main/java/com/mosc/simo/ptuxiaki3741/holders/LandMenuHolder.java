package com.mosc.simo.ptuxiaki3741.holders;

import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.controllers.LandFileController;
import com.mosc.simo.ptuxiaki3741.controllers.LandImgController;
import com.mosc.simo.ptuxiaki3741.controllers.LandPointsController;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.enums.LandImgState;
import com.mosc.simo.ptuxiaki3741.enums.LandPointsState;

public class LandMenuHolder implements NavigationView.OnNavigationItemSelectedListener{
    private final LandMapHolder mapHolder;
    private final LandViewHolder viewHolder;
    private final LandFileController fileController;
    private final LandPointsController pointsController;
    private final LandImgController imgController;

    public LandMenuHolder(LandViewHolder viewHolder, LandMapHolder mapHolder,
                          LandFileController file,LandPointsController point,LandImgController img){
        this.viewHolder = viewHolder;
        this.mapHolder = mapHolder;
        this.fileController = file;
        this.pointsController = point;
        this.imgController = img;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return menuItemClick(item.getItemId());
    }
    public boolean menuItemClick(int id) {
        mapHolder.clearFlag();
        viewHolder.closeDrawer();
        viewHolder.closeTabMenu();
        switch (id){
            case (R.id.menu_item_toggle_drawer):
                viewHolder.openDrawer();
                return true;
            case (R.id.menu_item_toggle_map_lock):
                mapHolder.toggleMapLock();
                return true;
            case(R.id.toolbar_action_add_on_end):
                mapHolder.onPointActionClick(LandPointsState.AddEnd);
                break;
            case(R.id.toolbar_action_add_between):
                mapHolder.onPointActionClick(LandPointsState.AddBetween);
                break;
            case(R.id.toolbar_action_edit):
                mapHolder.onPointActionClick(LandPointsState.Edit);
                break;
            case(R.id.toolbar_action_delete):
                mapHolder.onPointActionClick(LandPointsState.Delete);
                break;
            case(R.id.toolbar_action_clean):
                clearButtonMenu();
                break;
            case(R.id.toolbar_action_add_img):
                addOverlayButtonMenu();
                break;
            case(R.id.toolbar_action_remove_img):
                removeOverlayButtonMenu();
                break;
            case(R.id.toolbar_action_move_img):
                viewHolder.onImgActionClick(LandImgState.Move);
                break;
            case(R.id.toolbar_action_zoom_img):
                viewHolder.onImgActionClick(LandImgState.Zoom);
                break;
            case(R.id.toolbar_action_rotate_img):
                viewHolder.onImgActionClick(LandImgState.Rotate);
                break;
            case(R.id.toolbar_action_opacity_img):
                viewHolder.onImgActionClick(LandImgState.Alpha);
                break;
            case(R.id.toolbar_action_import):
                importButtonMenu();
                break;
            default:
                viewHolder.closeDrawer();
                return false;
        }
        viewHolder.closeDrawer();
        return true;
    }
    public void clearControllersFlags() {
        pointsController.setFlag(LandPointsState.Disable);
        imgController.setFlag(LandImgState.Disable);
        fileController.setFlag(LandFileState.Disable);
    }

    private void clearButtonMenu(){
        pointsController.clearList();
        mapHolder.drawOnMap();
        clearControllersFlags();
    }

    private void importButtonMenu(){
        if(fileController.checkPermissionGranted()){
            fileController.importMenuAction();
        }else{
            fileController.setFlag(LandFileState.File);
            fileController.requestPermission();
        }
    }
    private void addOverlayButtonMenu(){
        if(fileController.checkPermissionGranted()){
            fileController.addImgOverlay();
        }else{
            fileController.setFlag(LandFileState.Img);
            fileController.requestPermission();
        }
    }
    private void removeOverlayButtonMenu(){
        viewHolder.removeOverlayImg();
    }
}
