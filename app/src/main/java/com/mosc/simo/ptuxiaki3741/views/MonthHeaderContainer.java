package com.mosc.simo.ptuxiaki3741.views;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.databinding.CalendarMonthHeaderLayoutBinding;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public class MonthHeaderContainer extends ViewContainer {
    private final LinearLayout daysContainer;
    private final TextView day1,day2,day3,day4,day5,day6,day7;
    public MonthHeaderContainer(@NonNull View view) {
        super(view);
        CalendarMonthHeaderLayoutBinding binding = CalendarMonthHeaderLayoutBinding.bind(view);
        daysContainer = binding.daysContainer;
        day1 = binding.day1;
        day2 = binding.day2;
        day3 = binding.day3;
        day4 = binding.day4;
        day5 = binding.day5;
        day6 = binding.day6;
        day7 = binding.day7;
    }

    public void setDaysHeader(DayOfWeek[] daysOfWeek) {
        if(daysOfWeek.length != 7){
            daysContainer.setVisibility(View.GONE);
        }else{
            daysContainer.setVisibility(View.VISIBLE);
            String[] days = new String[7];
            for(int i = 0; i < 7; i++){
                days[i] = daysOfWeek[i].getDisplayName(TextStyle.SHORT, Locale.getDefault());
            }
            day1.setText(days[0]);
            day2.setText(days[1]);
            day3.setText(days[2]);
            day4.setText(days[3]);
            day5.setText(days[4]);
            day6.setText(days[5]);
            day7.setText(days[6]);
        }
    }
}
