package com.mosc.simo.ptuxiaki3741.data.util;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;

import java.util.Calendar;

public final class DialogUtil {

    private DialogUtil(){}

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

    public static MaterialAlertDialogBuilder getColorPickerDialog(Context context){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog);
        builder.setIcon(R.drawable.ic_menu_color_palette);
        builder.setTitle(context.getString(R.string.pick_color));
        builder.setView(R.layout.view_color_picker);
        return builder;
    }

    public static void setupColorDialog(AlertDialog dialog, ColorData tempColor) {
        Slider redSlider = dialog.findViewById(R.id.slRedSlider);
        Slider greenSlider = dialog.findViewById(R.id.slGreenSlider);
        Slider blueSlider = dialog.findViewById(R.id.slBlueSlider);
        FrameLayout colorBg = dialog.findViewById(R.id.flColorShower);

        if(colorBg != null){
            colorBg.setBackgroundColor(tempColor.getColor());
        }

        if(redSlider != null){
            redSlider.setValue(tempColor.getRed());
            redSlider.addOnChangeListener((range,value,user) -> {
                tempColor.setRed(Math.round(value));
                if(colorBg != null){
                    colorBg.setBackgroundColor(tempColor.getColor());
                }
            });
        }

        if(greenSlider != null){
            greenSlider.setValue(tempColor.getGreen());
            greenSlider.addOnChangeListener((range,value,user) -> {
                tempColor.setGreen(Math.round(value));
                if(colorBg != null){
                    colorBg.setBackgroundColor(tempColor.getColor());
                }
            });
        }

        if(blueSlider != null){
            blueSlider.setValue(tempColor.getBlue());
            blueSlider.addOnChangeListener((range,value,user) -> {
                tempColor.setBlue(Math.round(value));
                if(colorBg != null){
                    colorBg.setBackgroundColor(tempColor.getColor());
                }
            });
        }

        MaterialCardView mcvColor1 = dialog.findViewById(R.id.mcvColor1);
        MaterialCardView mcvColor2 = dialog.findViewById(R.id.mcvColor2);
        MaterialCardView mcvColor3 = dialog.findViewById(R.id.mcvColor3);
        MaterialCardView mcvColor4 = dialog.findViewById(R.id.mcvColor4);
        MaterialCardView mcvColor5 = dialog.findViewById(R.id.mcvColor5);
        MaterialCardView mcvColor6 = dialog.findViewById(R.id.mcvColor6);
        MaterialCardView mcvColor7 = dialog.findViewById(R.id.mcvColor7);
        MaterialCardView mcvColor8 = dialog.findViewById(R.id.mcvColor8);

        if(mcvColor1 != null){
            mcvColor1.setOnClickListener(v->{
                tempColor.setColor(mcvColor1.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor2 != null){
            mcvColor2.setOnClickListener(v->{
                tempColor.setColor(mcvColor2.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor3 != null){
            mcvColor3.setOnClickListener(v->{
                tempColor.setColor(mcvColor3.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor4 != null) {
            mcvColor4.setOnClickListener(v -> {
                tempColor.setColor(mcvColor4.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor5 != null){
            mcvColor5.setOnClickListener(v->{
                tempColor.setColor(mcvColor5.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor6 != null){
            mcvColor6.setOnClickListener(v->{
                tempColor.setColor(mcvColor6.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor7 != null){
            mcvColor7.setOnClickListener(v->{
                tempColor.setColor(mcvColor7.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }

        if(mcvColor8 != null){
            mcvColor8.setOnClickListener(v->{
                tempColor.setColor(mcvColor8.getCardBackgroundColor().getDefaultColor());
                changeValueBasedColor(tempColor, redSlider, greenSlider, blueSlider, colorBg);
            });
        }
    }

    private static void changeValueBasedColor(ColorData tempColor, Slider redSlider, Slider greenSlider, Slider blueSlider, FrameLayout colorBg) {
        if (redSlider != null) {
            redSlider.setValue(tempColor.getRed());
        }
        if (greenSlider != null) {
            greenSlider.setValue(tempColor.getGreen());
        }
        if (blueSlider != null) {
            blueSlider.setValue(tempColor.getBlue());
        }
        if (colorBg != null) {
            colorBg.setBackgroundColor(tempColor.getColor());
        }
    }

}
