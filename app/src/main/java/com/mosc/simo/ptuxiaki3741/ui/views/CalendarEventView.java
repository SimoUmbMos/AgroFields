package com.mosc.simo.ptuxiaki3741.ui.views;

import android.content.Context;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewCalendarListEventBinding;

public class CalendarEventView extends ConstraintLayout {
    private final ViewCalendarListEventBinding binding;
    public CalendarEventView(Context context) {
        super(context);
        binding = ViewCalendarListEventBinding.bind(
                inflate(getContext(), R.layout.view_calendar_list_event, this)
        );
    }
    public CalendarEventView(View view) {
        super(view.getContext());
        binding = ViewCalendarListEventBinding.bind(view);
    }
    public void setEvent(String type, Integer color, String title){
        binding.tvEventType.setText(type);
        binding.tvEventTitle.setText(title);
        if(color != null){
            binding.mcvRoot.setCardBackgroundColor(color);
        }
    }
    public void setOnClick(OnClickListener l){
        binding.mcvRoot.setOnClickListener(l);
    }
}
