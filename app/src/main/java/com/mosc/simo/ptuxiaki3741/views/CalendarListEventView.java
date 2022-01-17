package com.mosc.simo.ptuxiaki3741.views;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewCalendarListEventBinding;

public class CalendarListEventView extends ConstraintLayout {
    private final ViewCalendarListEventBinding binding;
    public CalendarListEventView(Context context) {
        super(context);
        binding = ViewCalendarListEventBinding.bind(
                inflate(getContext(), R.layout.view_calendar_list_event, this)
        );
        binding.mcvRoot.setCardElevation(0);
    }
    public void setTitle(String title){
        binding.tvEventTitle.setText(title);
    }
    public void setOnClick(OnClickListener l){
        binding.mcvRoot.setOnClickListener(l);
    }
}
