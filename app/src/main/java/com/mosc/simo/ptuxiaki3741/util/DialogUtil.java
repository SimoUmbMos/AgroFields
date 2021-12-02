package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mosc.simo.ptuxiaki3741.R;

public final class DialogUtil {
    private DialogUtil(){}
    public static MaterialAlertDialogBuilder getColorPickerDialog(Context context){
        return new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog)
                .setTitle(context.getString(R.string.pick_color))
                .setView(R.layout.view_color_picker);
    }
}
