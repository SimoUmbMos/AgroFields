package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandActionStates;

import java.util.ArrayList;
import java.util.List;

public class LandPointsController {
    public static final double DefaultDistanceToAction = 150000;

    private double distanceToAction;
    private LandActionStates state;
    private List<LatLng> points;
    private final List<List<LatLng>> undoList;

    private int index1=-1,index2=-1,index3=-1,editIndex=-1;

    public LandPointsController(){
        distanceToAction = DefaultDistanceToAction;
        state = LandActionStates.Disable;
        points=new ArrayList<>();
        undoList=new ArrayList<>();
    }

    public LandActionStates getFlag() {
        return state;
    }
    public int getEditIndex() {
        return editIndex;
    }
    public List<LatLng> getPoints() {
        return points;
    }
    public void setPoints(List<LatLng> latLng) {
        points = new ArrayList<>(latLng);
        clearUndo();
        resetAddBetweenStatus();
        resetEditStatus();
    }

    public void setFlag(LandActionStates state) {
        if(this.state != state){
            resetAddBetweenStatus();
            resetEditStatus();
        }
        this.state = state;
    }

    public boolean isActive() {
        switch (state){
            case AddEnd:
            case AddBetween:
            case Edit:
            case Delete:
                return true;
            default:
                return false;
        }
    }

    public void clearList() {
        points.clear();
        undoList.clear();
        resetAddBetweenStatus();
        resetEditStatus();
    }

    private void addToUndo() {
        if(undoList.size()>0){
            if(!points.equals(undoList.get(undoList.size()-1))){
                List<LatLng> temp = new ArrayList<>(this.points);
                undoList.add(temp);
            }
        }else{
            List<LatLng> temp = new ArrayList<>(this.points);
            undoList.add(temp);
        }
    }
    public void clearUndo() {
        undoList.clear();
        resetAddBetweenStatus();
        resetEditStatus();
    }
    public void undo() {
        if(undoList.size()>0){
            switch (state){
                case AddBetween:
                    points.clear();
                    points.addAll(undoList.get(undoList.size()-1));
                    resetAddBetweenStatus();
                    break;
                case Edit:
                    points.clear();
                    points.addAll(undoList.get(undoList.size()-1));
                    resetEditStatus();
                    break;
                default:
                    points.clear();
                    points.addAll(undoList.get(undoList.size()-1));
                    undoList.remove(undoList.size()-1);
            }
        }
    }

    public void processClick(LatLng latLng) {
        switch (state){
            case AddEnd:
                addToUndo();
                addEndPoint(latLng);
                break;
            case AddBetween:
                if(points.size()>=2){
                    addBetweenPoint(latLng);
                }
                break;
            case Edit:
                addToUndo();
                editPoint(latLng);
                break;
            case Delete:
                addToUndo();
                deletePoint(latLng);
                break;
            default:
        }
    }

    private void addEndPoint(LatLng latLng) {
        points.add(latLng);
    }
    private void deletePoint(LatLng latLng) {
        LatLng closestPoint = findClosestPoint(latLng);
        if(distanceBetween(closestPoint,latLng)<=distanceToAction){
            points.remove(closestPoint);
        }
    }
    private void editPoint(LatLng latLng) {
        if(editIndex == -1){
            undoList.clear();
            addToUndo();
            LatLng closestPoint = findClosestPoint(latLng);
            if(distanceBetween(closestPoint,latLng)<=(distanceToAction*2.5)){
                editIndex = points.indexOf(closestPoint);
            }
        }else{
            points.set(editIndex,latLng);
        }
    }

    private void addBetweenPoint(LatLng latLng) {
        switch (getAddBetweenStatus()){
            case 1:
                selectFirstPointForBetween(latLng);
                break;
            case 2:
                selectSecondPointForBetween(latLng);
                break;
            case 3:
                placePointBetween(latLng);
                break;
            case 4:
                editPointBetween(latLng);
                break;
        }
    }
    private void selectFirstPointForBetween(LatLng latLng) {
        undoList.clear();
        addToUndo();
        LatLng between1 = findClosestPoint(latLng);
        index1 = points.indexOf(between1);
        if(distanceBetween(latLng,between1) > distanceToAction){
            index1 = -1;
        }
    }
    private void selectSecondPointForBetween(LatLng latLng) {
        LatLng between2 = findClosestPoint(latLng);
        index2 = points.indexOf(between2);
        int temp;
        if(index1 > index2){
            temp = index1;
            index1 = index2;
            index2 = temp;
        }
        if(distanceBetween(latLng,between2) > distanceToAction){
            index2 = -1;
        }
        checkIndexBetween();
    }
    private void placePointBetween(LatLng latLng) {
        if(index2 == (points.size() - 1)){
            if(index1 == 0){
                index3=points.size();
                points.add(latLng);
            }else{
                index3=index2;
                index2++;
                points.add(index3,latLng);
            }
        }else{
            index3=index2;
            index2++;
            points.add(index3,latLng);
        }
    }
    private void editPointBetween(LatLng latLng) {
        points.set(index3,latLng);
    }
    private void checkIndexBetween() {
        boolean reset = true;
        if(index2>-1){
            int temp = index1 + 1;
            int last = points.size() - 1;
            if(temp == index2){
                reset = false;
            }else if(index1 == 0 && index2 == last){
                reset = false;
            }
            if(index2 == index1){
                reset = true;
            }
        }
        if(reset)
            resetAddBetweenStatus();
    }
    private void resetAddBetweenStatus(){
        index1=-1;
        index2=-1;
        index3=-1;
    }
    private void resetEditStatus(){
        editIndex=-1;
    }
    public int getAddBetweenStatus() {
        int ans;
        if(index1 == -1){
            ans = 1;
        }else if(index2 == -1){
            ans = 2;
        }else if(index3 == -1){
            ans = 3;
        }else{
            ans = 4;
        }
        return ans;
    }
    private LatLng findClosestPoint(LatLng latLng){
        LatLng result = null;
        double smallestDistance = -1,
                currDistance;
        for(LatLng point : points){
            currDistance=distanceBetween(latLng, point);
            if(smallestDistance == -1 || currDistance < smallestDistance){
                result = point;
                smallestDistance = currDistance;
            }
        }
        return result;
    }
    private static double distanceBetween(LatLng first, LatLng second) {
        float[] distance = new float[1];
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, distance);
        return distance[0];
    }

    public double getDistanceToAction() {
        return distanceToAction;
    }
    public void setDistanceToAction(double distanceToAction) {
        this.distanceToAction = distanceToAction;
    }

    public void addAll(List<LatLng> points) {
        clearList();
        clearUndo();
        resetAddBetweenStatus();
        resetEditStatus();
        state = LandActionStates.Disable;
        this.points.addAll(points);
    }
}
