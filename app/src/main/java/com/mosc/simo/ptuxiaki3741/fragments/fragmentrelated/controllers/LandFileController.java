package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.helper.FileHelper;

import java.util.List;

public class LandFileController {
    private LandFileState state = LandFileState.Disable;

    private final Activity activity;
    private final FileHelper fileHelper;
    private final ActivityResultLauncher<Intent> imgResultLauncher,fileResultLauncher;
    private final ActivityResultLauncher<String> prl;

    public LandFileController(Activity activity, ActivityResultLauncher<String> prl,
                              ActivityResultLauncher<Intent> imgResultLauncher,
                              ActivityResultLauncher<Intent> fileResultLauncher){
        this.activity = activity;
        this.fileHelper = new FileHelper(activity);
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
        return fileHelper.fileIsValid(response);
    }
    public boolean fileIsValidImg(Intent response){
        return fileHelper.fileIsValidImg(response);
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
        return fileHelper.handleFile(result);
    }

    public LandFileState getFlag() {
        return state;
    }
}
