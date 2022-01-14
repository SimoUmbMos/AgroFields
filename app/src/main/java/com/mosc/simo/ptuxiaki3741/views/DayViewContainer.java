package com.mosc.simo.ptuxiaki3741.views;

import android.view.View;

import androidx.annotation.NonNull;

import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.CalendarDayLayoutBinding;

public class DayViewContainer extends ViewContainer {
    private final CalendarDayLayoutBinding binding;
    private boolean today;
    public DayViewContainer(@NonNull View view) {
        super(view);
        binding = CalendarDayLayoutBinding.bind(view);
        today = false;
    }

    public void setText(String text){
        binding.calendarDayText.setText(text);
    }
    public void setToday(boolean today){
        this.today = today;
    }
    public void setText(int res){
        binding.calendarDayText.setText(res);
    }
    public void setBadgeCount(int num) {
        if(num >= 0){
            switch (num){
                case 0:
                    binding.calendarBadge1.setVisibility(View.GONE);
                    binding.calendarBadge2.setVisibility(View.GONE);
                    binding.calendarBadge3.setVisibility(View.GONE);
                    binding.calendarBadge4.setVisibility(View.GONE);
                    binding.calendarBadgeMore.setVisibility(View.GONE);
                    break;
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
            binding.calendarBadge1.setVisibility(View.GONE);
            binding.calendarBadge2.setVisibility(View.GONE);
            binding.calendarBadge3.setVisibility(View.GONE);
            binding.calendarBadge4.setVisibility(View.GONE);
            binding.calendarBadgeMore.setVisibility(View.GONE);
        }
    }
    public void setOnClick(View.OnClickListener l){
        binding.getRoot().setOnClickListener(l);
    }
    public void setEnable(boolean enable) {
        binding.getRoot().setClickable(enable);
        if(enable){
            binding.calendarDayText.setVisibility(View.VISIBLE);
            binding.badgeContainer.setVisibility(View.VISIBLE);
            if (today) {
                binding.getRoot().setCardBackgroundColor(
                        binding.getRoot().getContext().getColor(R.color.cardBackgroundCalendarToday)
                );
            }else{
                binding.getRoot().setCardBackgroundColor(
                        binding.getRoot().getContext().getColor(R.color.cardBackgroundCalendarEnable)
                );
            }
        }else{
            binding.calendarDayText.setVisibility(View.GONE);
            binding.badgeContainer.setVisibility(View.GONE);
            binding.getRoot().setCardBackgroundColor(
                    binding.getRoot().getContext().getColor(R.color.cardBackgroundCalendarDisable)
            );
        }
    }
}
