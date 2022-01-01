package com.mosc.simo.ptuxiaki3741.views;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.databinding.CalendarMonthHeaderLayoutBinding;

public class MonthHeaderContainer extends ViewContainer {
    public final TextView headerTextView;
    public final Button headerPreviousMonth;
    public final Button headerNextMonth;
    public MonthHeaderContainer(@NonNull View view) {
        super(view);
        CalendarMonthHeaderLayoutBinding binding = CalendarMonthHeaderLayoutBinding.bind(view);
        headerTextView = binding.headerTextView;
        headerPreviousMonth = binding.headerPreviousMonth;
        headerNextMonth = binding.headerNextMonth;
    }
}
