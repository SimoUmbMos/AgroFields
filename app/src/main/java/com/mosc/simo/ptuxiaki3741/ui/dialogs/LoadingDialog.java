package com.mosc.simo.ptuxiaki3741.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

import androidx.annotation.NonNull;

import com.mosc.simo.ptuxiaki3741.R;

public class LoadingDialog {
    private final Activity activity;
    private final AlertDialog dialog;

    public LoadingDialog(@NonNull Activity activity){
        this.activity = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(View.inflate(activity, R.layout.dialog_loading, null));
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
