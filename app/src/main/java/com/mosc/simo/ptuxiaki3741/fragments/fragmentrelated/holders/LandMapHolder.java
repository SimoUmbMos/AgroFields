package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers.LandPointsController;
import com.mosc.simo.ptuxiaki3741.enums.LandPointsState;
import com.mosc.simo.ptuxiaki3741.interfaces.OnAction;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;

import java.util.List;

public class LandMapHolder implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraMoveListener, OnAction {

    private final LandViewHolder viewHolder;
    private final LandPointsController pointsController;

    private GoogleMap mMap;
    private boolean mapIsLocked = false;

    public LandMapHolder(LandViewHolder viewHolder, LandPointsController pointsController) {
        this.viewHolder = viewHolder;
        this.pointsController = pointsController;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
        mMap.setOnMapLoadedCallback(this::mapFullLoaded);
    }

    private void initMap() {
        mMap.setMinZoomPreference(5);
        mMap.setMaxZoomPreference(20);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.983810,23.727539),5));

        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraMoveListener(this);
    }

    private void mapFullLoaded() {
        if(pointsController.getPoints().size()>0)
            drawOnMap();
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        pointsController.processClick(latLng);
        drawOnMap();
        changeTitleBasedOnAction();
    }
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        onMapClick(latLng);
    }
    @Override
    public void onCameraMove() {
        drawOnMap();
    }
    public void clearFlag() {
        pointsController.setFlag(LandPointsState.Disable);
    }

    public void toggleMapLock(){
        if(mMap != null){
            if(mapIsLocked){
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mapIsLocked=false;
                if(viewHolder.miLock != null){
                    viewHolder.miLock.setIcon(R.drawable.menu_ic_unlocked);
                }
            }else{
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                mapIsLocked=true;
                if(viewHolder.miLock != null){
                    viewHolder.miLock.setIcon(R.drawable.menu_ic_locked);
                }
            }
        }
    }
    private void changeDistanceToActionBasedZoom() {
        float zoom = mMap.getCameraPosition().zoom;
        double distance=16;
        if(zoom>16){
            distance = 20-zoom;
            distance = Math.pow(2,distance);
        }
        pointsController.setDistanceToAction(distance);
    }
    public void drawOnMap() {
        mMap.clear();
        if(pointsController.getPoints().size()>0){
            mMap.addPolygon(new PolygonOptions().addAll(pointsController.getPoints()));
            changeDistanceToActionBasedZoom();
            for(LatLng point : pointsController.getPoints()){
                mMap.addCircle(new CircleOptions()
                        .center(point)
                        .radius(pointsController.getDistanceToAction()));
            }
        }
    }
    private void ZoomToPoints() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : pointsController.getPoints()){
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 8));
    }

    public void onPointActionClick(LandPointsState flagPointController) {
        pointsController.clearUndo();
        pointsController.setFlag(flagPointController);
        switch (flagPointController){
            case AddEnd:
                viewHolder.showTabMenu(
                        "Add To End",
                        save->viewHolder.save(this),
                        undo->viewHolder.undo(this)
                );
                break;
            case AddBetween:
                viewHolder.showTabMenu(
                        "Select First Point",
                        save->viewHolder.save(this),
                        undo->viewHolder.undo(this)
                );
                break;
            case Edit:
                viewHolder.showTabMenu(
                        "Select Point To Edit",
                        save->viewHolder.save(this),
                        undo->viewHolder.undo(this)
                );
                break;
            case Delete:
                viewHolder.showTabMenu(
                        "Delete Points",
                        save->viewHolder.save(this),
                        undo->viewHolder.undo(this)
                );
                break;
        }
    }
    private void changeTitleBasedOnAction() {
        if (pointsController.getFlag() == LandPointsState.AddBetween){
            switch (pointsController.getAddBetweenStatus()) {
                case (1):
                    viewHolder.changeTabMenuTitle("Select First Point");
                    break;
                case (2):
                    viewHolder.changeTabMenuTitle("Select Second Point");
                    break;
                case (3):
                    viewHolder.changeTabMenuTitle("Place Point");
                    break;
                case (4):
                    viewHolder.changeTabMenuTitle("Edit Placed Point");
                    break;
            }
        }else if(pointsController.getFlag() == LandPointsState.Edit){
            if(pointsController.getEditIndex() == -1){
                viewHolder.changeTabMenuTitle("Select Point To Edit");
            }else{
                viewHolder.changeTabMenuTitle("Edit Point");
            }
        }
    }

    @Override
    public void onSave() {
        clearFlag();
        if(mapIsLocked)
            toggleMapLock();
    }
    @Override
    public void onUndo() {
        if(pointsController.isActive()){
            pointsController.undo();
            drawOnMap();
            changeTitleBasedOnAction();
        }
    }

    public void onActivityResult(Intent result) {
        ParcelablePolygon polygon =(ParcelablePolygon) result.getExtras().get(ImportActivity.resName);
        List<LatLng> points = polygon.getPoints();
        pointsController.addAll(points);
        drawOnMap();
        ZoomToPoints();
    }
}
