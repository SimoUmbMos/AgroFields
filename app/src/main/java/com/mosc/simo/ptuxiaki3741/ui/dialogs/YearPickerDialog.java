package com.mosc.simo.ptuxiaki3741.ui.dialogs;

import android.app.Activity;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.time.LocalDate;

public class YearPickerDialog {
    private final Activity activity;
    private final AlertDialog dialog;
    private final String defaultTitle;
    private NumberPicker numberPicker;
    private TextView tvTitle;

    public YearPickerDialog(@NonNull Activity activity, ActionResult<Long> onSuccess){
        this.activity = activity;
        defaultTitle = activity.getString(R.string.default_year_picker_title);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.activity, R.style.MaterialAlertDialog_Tint);
        builder.setView(R.layout.dialog_year_picker);
        builder.setOnDismissListener((d)->closeDialog());
        builder.setNeutralButton(R.string.cancel, (d,i)->d.dismiss());
        builder.setNegativeButton(R.string.current_year, (d,i)-> onSuccess.onActionResult((long) LocalDate.now().getYear()));
        builder.setPositiveButton(R.string.submit,(d,i)-> {
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
                tvTitle = null;
                numberPicker = null;
            }
            dialog.show();
            tvTitle = dialog.findViewById(R.id.tvTitle);
            if(tvTitle != null){
                tvTitle.setText(defaultTitle);
            }
            numberPicker = dialog.findViewById(R.id.npYearPicker);
            if(numberPicker != null){
                numberPicker.setMinValue((int)AppValues.minSnapshot);
                numberPicker.setMaxValue((int)AppValues.maxSnapshot);
                numberPicker.setValue((int)snapshot);
            }
        });
    }

    public void openDialog(String title, long snapshot){
        activity.runOnUiThread(()->{
            if(dialog.isShowing()) {
                dialog.dismiss();
                numberPicker = null;
            }
            dialog.show();
            tvTitle = dialog.findViewById(R.id.tvTitle);
            if(tvTitle != null){
                if(title != null) tvTitle.setText(title);
                else tvTitle.setText(defaultTitle);
            }
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
            tvTitle = null;
            numberPicker = null;
        });
    }

    public AlertDialog getDialog() {
        return dialog;
    }
}
