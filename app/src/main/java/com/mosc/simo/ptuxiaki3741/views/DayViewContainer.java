package com.mosc.simo.ptuxiaki3741.views;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.databinding.CalendarDayLayoutBinding;

public class DayViewContainer extends ViewContainer {
    public final ConstraintLayout root;
    public final TextView calendarDayText;
    public final View calendarDayBadge;
    public DayViewContainer(@NonNull View view) {
        super(view);
        CalendarDayLayoutBinding binding = CalendarDayLayoutBinding.bind(view);
        root = binding.getRoot();
        calendarDayText = binding.calendarDayText;
        calendarDayBadge = binding.calendarDayBadge;
    }
}
