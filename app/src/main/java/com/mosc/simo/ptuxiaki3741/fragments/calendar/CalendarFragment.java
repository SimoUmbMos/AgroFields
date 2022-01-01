package com.mosc.simo.ptuxiaki3741.fragments.calendar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarBinding;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.views.DayViewContainer;
import com.mosc.simo.ptuxiaki3741.views.MonthHeaderContainer;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    //fixme: create fragment
    private FragmentCalendarBinding binding;
    private YearMonth currentMonth;
    private DayOfWeek firstDayOfWeek;
    private List<CalendarNotification> notificationList;
    private int textColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentCalendarBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewModel();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initData(){
        notificationList = new ArrayList<>();
        currentMonth = YearMonth.now();
        firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        if(getContext() != null){
            textColor = ContextCompat.getColor(getContext(),R.color.textColor);
        }else{
            textColor = AppValues.defaultDayColor1;
        }
    }
    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(()-> true);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.calendar_fragment_title));
                actionBar.show();
            }
        }
    }
    private void initFragment(){
        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));
        initCalendar();
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }
    private void initCalendar() {
        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }
            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.calendarDayText.setText(
                        String.valueOf(day.getDate().getDayOfMonth())
                );
                container.calendarDayBadge.setVisibility(View.GONE);
                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    container.calendarDayText.setTextColor(textColor);
                    if(notificationList.size()>0){
                        final int dayOFYear = day.getDate().getDayOfYear();
                        final int year = day.getDate().getYear();
                        final Calendar calendar = Calendar.getInstance();
                        int tempDayOfYear;
                        int tempYear;
                        for(CalendarNotification notification : notificationList){
                            calendar.setTime(notification.getDate());
                            tempDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                            tempYear = calendar.get(Calendar.YEAR);
                            if( tempDayOfYear == dayOFYear && tempYear == year ){
                                container.calendarDayBadge.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                }else{
                    container.calendarDayText.setTextColor(AppValues.defaultDayColor2);
                }
            }
        });
        binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderContainer>() {
            @NonNull
            @Override
            public MonthHeaderContainer create(@NonNull View view) {
                return new MonthHeaderContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderContainer container, @NonNull CalendarMonth month) {
                container.headerTextView.setText(String.format(Locale.getDefault(), "%s %d",
                        getMonth(month.getMonth()), month.getYear()
                ));
                container.headerPreviousMonth.setOnClickListener(v->
                        binding.calendarView.scrollToMonth(month.getYearMonth().minusMonths(1))
                );
                container.headerNextMonth.setOnClickListener(v->
                        binding.calendarView.scrollToMonth(month.getYearMonth().plusMonths(1))
                );
            }
        });
        YearMonth firstMonth = currentMonth.minusMonths(24);
        YearMonth lastMonth = currentMonth.plusMonths(24);
        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        binding.calendarView.scrollToMonth(currentMonth);
    }

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month-1];
    }

    private void toNewEvent(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                if(nav != null)
                    nav.navigate(R.id.toCalendarNewEvent);
            });
    }
}