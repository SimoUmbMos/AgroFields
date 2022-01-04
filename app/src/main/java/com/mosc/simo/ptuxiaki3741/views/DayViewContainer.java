package com.mosc.simo.ptuxiaki3741.views;

import android.view.View;

import androidx.annotation.NonNull;

import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.databinding.CalendarDayLayoutBinding;

public class DayViewContainer extends ViewContainer {
    private final CalendarDayLayoutBinding binding;
    public DayViewContainer(@NonNull View view) {
        super(view);
        binding = CalendarDayLayoutBinding.bind(view);
    }

    public void setText(String text){
        binding.calendarDayText.setText(text);
    }
    public void setText(int res){
        binding.calendarDayText.setText(res);
    }
    public void setBadgeCount(int num) {
        if(num > 0){
            binding.badgeContainer.setVisibility(View.VISIBLE);
            switch (num){
                case 1:
                    binding.calendarBadge1.setVisibility(View.VISIBLE);
                    binding.calendarBadge2.setVisibility(View.GONE);
                    binding.calendarBadge3.setVisibility(View.GONE);
                    binding.calendarBadge4.setVisibility(View.GONE);
                    binding.calendarBadgeMore.setVisibility(View.GONE);
                    break;
                case 2:
                    binding.calendarBadge1.setVisibility(View.VISIBLE);
                    binding.calendarBadge2.setVisibility(View.VISIBLE);
                    binding.calendarBadge3.setVisibility(View.GONE);
                    binding.calendarBadge4.setVisibility(View.GONE);
                    binding.calendarBadgeMore.setVisibility(View.GONE);
                    break;
                case 3:
                    binding.calendarBadge1.setVisibility(View.VISIBLE);
                    binding.calendarBadge2.setVisibility(View.VISIBLE);
                    binding.calendarBadge3.setVisibility(View.VISIBLE);
                    binding.calendarBadge4.setVisibility(View.GONE);
                    binding.calendarBadgeMore.setVisibility(View.GONE);
                    break;
                case 4:
                    binding.calendarBadge1.setVisibility(View.VISIBLE);
                    binding.calendarBadge2.setVisibility(View.VISIBLE);
                    binding.calendarBadge3.setVisibility(View.VISIBLE);
                    binding.calendarBadge4.setVisibility(View.VISIBLE);
                    binding.calendarBadgeMore.setVisibility(View.GONE);
                    break;
                default:
                    binding.calendarBadge1.setVisibility(View.VISIBLE);
                    binding.calendarBadge2.setVisibility(View.VISIBLE);
                    binding.calendarBadge3.setVisibility(View.VISIBLE);
                    binding.calendarBadge4.setVisibility(View.VISIBLE);
                    binding.calendarBadgeMore.setVisibility(View.VISIBLE);
                    break;
            }
        }else{
            binding.badgeContainer.setVisibility(View.GONE);
        }
    }
    public void setOnClick(View.OnClickListener l){
        binding.getRoot().setOnClickListener(l);
    }
}
