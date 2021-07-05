package com.mosc.simo.ptuxiaki3741.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.util.file.extensions.kml.KmlFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LandFileController {
    private LandFileState state = LandFileState.Disable;

    private final Activity activity;
    private final ActivityResultLauncher<Intent> imgResultLauncher,fileResultLauncher;
    private final ActivityResultLauncher<String> prl;

    public LandFileController(Activity activity, ActivityResultLauncher<String> prl,
                              ActivityResultLauncher<Intent> imgResultLauncher,
                              ActivityResultLauncher<Intent> fileResultLauncher){
        this.activity = activity;
        this.imgResultLauncher = imgResultLauncher;
        this.fileResultLauncher = fileResultLauncher;
        this.prl = prl;
    }

    public Intent getFilePickerIntent(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        if(state == LandFileState.Img){
            chooseFile.setType("image/*");
        }else{
            chooseFile.setType("*/*");
        }
        return chooseFile;
    }

    public boolean fileIsValid(Intent response){
        return isKML(response) || isJSON(response) || isGML(response);
    }
    public boolean fileIsValidImg(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("png") || extension.equals("jpg");
    }
    public boolean isKML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("kml");
    }
    public boolean isJSON(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("json");
    }
    public boolean isGML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("gml");
    }

    private String getFileName(Intent response) {
        String displayName = "";
        Uri uri = response.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);

        if (uriString.startsWith("content://")) {
            try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        return displayName;
    }
    private String getExtension(String s){
        String[] segments = s.split("\\.");
        if(segments.length>0)
            return segments[segments.length-1];
        else
            return "";
    }

    public void importMenuAction() {
        state = LandFileState.File;
        fileResultLauncher.launch(getFilePickerIntent());
    }
    public void addImgOverlay() {
        state = LandFileState.Img;
        imgResultLauncher.launch(getFilePickerIntent());
    }

    public void setFlag(LandFileState state){
        this.state=state;
    }

    public boolean checkPermissionGranted(){
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if(activity != null)
            return activity.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED;
        else
            return false;
    }

    public void requestPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        prl.launch(permission);
    }

    public List<List<LatLng>> handleFile(Intent result) {
        if(fileIsValid(result)){
            Uri uri = result.getData();
            if(isKML(result)){
                return handleKml(uri);
            }else if(isJSON(result)){
                return handleJson(uri);
            }
        }
        return new ArrayList<>();
    }

    private List<List<LatLng>> handleKml(Uri uri) {
        try{
            return KmlFileReader.execOnMainThread(activity.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private List<List<LatLng>> handleJson(Uri uri) {
        try{
            return GeoJsonReader.execOnMainThread(activity.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public LandFileState getFlag() {
        return state;
    }
}
