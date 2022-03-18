package com.mosc.simo.ptuxiaki3741.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
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
    public void setEvent(String type, ColorData color, String title){
        binding.tvEventType.setText(type);
        binding.tvEventTitle.setText(title);
        if(color != null){
            binding.mcvRoot.setCardBackgroundColor(color.getColor());
            double luminance = UIUtil.getColorLuminance(color);
            if(luminance > 0.179){
                binding.tvEventType.setTextColor(Color.BLACK);
                binding.tvEventTitle.setTextColor(Color.BLACK);
            }else{
                binding.tvEventType.setTextColor(Color.WHITE);
                binding.tvEventTitle.setTextColor(Color.WHITE);
            }
        }
    }
    public void setOnClick(OnClickListener l){
        binding.mcvRoot.setOnClickListener(l);
    }
}
