package com.mosc.simo.ptuxiaki3741.fragments.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;

public class MapFileController {
    private static final String TAG = "MapFileController";
    public static final int
            READ_EXTERNAL_STORAGE = 41,
            FILE_PICKER_REQUEST_CODE = 42,
            IMG_PICKER_REQUEST_CODE = 43,
            flagDisable = 0,
            flagFile = 1,
            flagImg = 2;
    private int flag = flagDisable;

    private final MapImgController mic;
    private final Activity activity;

    public MapFileController(MapImgController mic, Activity activity){
        this.mic = mic;
        this.activity = activity;
    }

    public Intent getFilePickerIntent(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        if(flag == flagImg){
            chooseFile.setType("image/*");
        }else if(flag == flagFile){
            chooseFile.setType("file/*");
        }else{
            chooseFile.setType("*/*");
        }
        return chooseFile;
    }

    private boolean fileIsValid(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("kml") || extension.equals("json") || extension.equals("gml");
    }
    private boolean IsKML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("kml");
    }
    private boolean IsJSON(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("json");
    }
    private boolean IsGML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("gml");
    }
    private boolean fileIsValidImg(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("png") || extension.equals("jpg");
    }

    private void handleFile(Intent response){
        if(IsKML(response)){
            kmlAction(response);
        }else if(IsJSON(response)){
            geoJsonAction(response);
        }else if(IsGML(response)){
            gmlAction(response);
        }
    }

    private void gmlAction(Intent response) {
        //todo
    }
    private void geoJsonAction(Intent response) {
        //todo
    }
    private void kmlAction(Intent response) {
        //todo
    }

    private void handleImg(Intent response){
        Uri uri = response.getData();
        mic.addNewOverlayImg();
        mic.showImage(uri);
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
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if(checkPermissionGranted(permission)){
            flag = MapFileController.flagFile;
            Intent intent = getFilePickerIntent();
            activity.startActivityForResult(intent,FILE_PICKER_REQUEST_CODE);
        }else{
            activity.requestPermissions(new String[]{permission},READ_EXTERNAL_STORAGE);
        }
    }
    public void addImgOverlay() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if(checkPermissionGranted(permission)){
            flag = MapFileController.flagImg;
            Intent intent = getFilePickerIntent();
            activity.startActivityForResult(intent,IMG_PICKER_REQUEST_CODE);
        }else{
            activity.requestPermissions(new String[]{permission},READ_EXTERNAL_STORAGE);
        }
    }

    //Permission's
    public boolean checkPermissionGranted(String permission){
        if(activity != null)
            return activity.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED;
        else
            return false;
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[0] + " granted");
            } else {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[0] + " denied");
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == FILE_PICKER_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                switch (flag){
                    case (flagFile):
                        if(fileIsValid(data))
                            handleFile(data);
                        break;
                    case (flagImg):
                        if(fileIsValidImg(data))
                            handleImg(data);
                        break;
                }
            }
            flag = flagDisable;
        }
    }
}
