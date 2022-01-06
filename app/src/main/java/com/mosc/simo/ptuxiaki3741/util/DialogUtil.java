package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mosc.simo.ptuxiaki3741.R;

import java.util.Calendar;

public final class DialogUtil {
    private DialogUtil(){}
    public static MaterialAlertDialogBuilder getColorPickerDialog(Context context){
        return new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog)
                .setTitle(context.getString(R.string.pick_color))
                .setView(R.layout.view_color_picker);
    }
    public static MaterialDatePicker<Long> getDatePickerDialog(String title, Calendar date){
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(title);
        builder.setSelection(date.getTimeInMillis());
        return builder.build();
    }
    public static MaterialTimePicker getTimePickerDialog(String title, Calendar date, boolean c24){
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
        builder.setTitleText(title);
        if( c24 ) {
            builder.setTimeFormat(TimeFormat.CLOCK_24H);
        } else {
            builder.setTimeFormat(TimeFormat.CLOCK_12H);
        }
        builder.setHour(date.get(Calendar.HOUR_OF_DAY));
        builder.setMinute(date.get(Calendar.MINUTE));
        return builder.build();
    }
}
