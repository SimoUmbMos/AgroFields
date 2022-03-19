package com.mosc.simo.ptuxiaki3741.ui.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.R;

public class LoadingDialog {
    private final Activity activity;
    private final AlertDialog dialog;

    public LoadingDialog(@NonNull Activity activity){
        this.activity = activity;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.activity, R.style.MaterialAlertDialog);
        builder.setView(R.layout.dialog_loading);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    public void openDialog(){
        activity.runOnUiThread(()->{
            if(!dialog.isShowing()) {
                dialog.show();
            }
        });
    }

    public void closeDialog(){
        activity.runOnUiThread(()->{
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        });
    }
}
