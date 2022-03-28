package com.mosc.simo.ptuxiaki3741.ui.dialogs;

import android.app.Activity;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

public class YearPickerDialog {
    private final Activity activity;
    private final AlertDialog dialog;
    private NumberPicker numberPicker;

    public YearPickerDialog(@NonNull Activity activity, ActionResult<Long> onSuccess){
        this.activity = activity;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.activity, R.style.MaterialAlertDialog);
        builder.setView(R.layout.dialog_year_picker);
        builder.setOnDismissListener((d)->closeDialog());
        builder.setNeutralButton(activity.getResources().getString(R.string.cancel),(d,i)->d.dismiss());
        builder.setPositiveButton(activity.getResources().getString(R.string.okey),(d,i)-> {
            if(numberPicker != null){
                onSuccess.onActionResult((long)numberPicker.getValue());
            }
        });
        dialog = builder.create();
    }

    public void openDialog(long snapshot){
        activity.runOnUiThread(()->{
            if(dialog.isShowing()) {
                dialog.dismiss();
                numberPicker = null;
            }
            dialog.show();
            numberPicker = dialog.findViewById(R.id.npYearPicker);
            if(numberPicker != null){
                numberPicker.setMinValue((int)AppValues.minSnapshot);
                numberPicker.setMaxValue((int)AppValues.maxSnapshot);
                numberPicker.setValue((int)snapshot);
            }
        });
    }

    public void closeDialog(){
        activity.runOnUiThread(()->{
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
            numberPicker = null;
        });
    }
}
