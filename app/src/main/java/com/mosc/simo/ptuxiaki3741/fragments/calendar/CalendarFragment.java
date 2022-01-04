package com.mosc.simo.ptuxiaki3741.fragments.calendar;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;
import com.mosc.simo.ptuxiaki3741.views.DayViewContainer;
import com.mosc.simo.ptuxiaki3741.views.MonthHeaderContainer;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;

public class CalendarFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarBinding binding;
    private YearMonth currentMonth;
    private DayOfWeek firstDayOfWeek;
    private Map<LocalDate, List<CalendarNotification>> notifications;

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
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initData(){
        notifications = new HashMap<>();
        currentMonth = YearMonth.now();
        firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.calendar_fragment_title));
                    actionBar.hide();
                }
            }
        }
    }
    private void initFragment(){
        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));
        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }
            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                if(day.getOwner() == DayOwner.THIS_MONTH){
                    container.setText(
                            String.valueOf(day.getDate().getDayOfMonth())
                    );
                    container.setOnClick(v->onDayClick(day.getDate()));
                    List<CalendarNotification> temp =
                            notifications.getOrDefault(day.getDate(),null);
                    if(temp != null){
                        container.setBadgeCount(temp.size());
                    }else{
                        container.setBadgeCount(0);
                    }
                }else{
                    container.setText("");
                    container.setOnClick(null);
                    container.setBadgeCount(0);
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
                        onCalendarUpdate(month.getYearMonth().minusMonths(1))
                );
                container.headerNextMonth.setOnClickListener(v->
                        onCalendarUpdate(month.getYearMonth().plusMonths(1))
                );
            }
        });
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }
    private void onCalendarUpdate(YearMonth currentMonth) {
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.calendarView.setupAsync(
                currentMonth,
                currentMonth,
                firstDayOfWeek,
                ()->{
                    binding.loadingView.setVisibility(View.GONE);
                    return Unit.INSTANCE;
                }
        );
    }
    private void onDayClick(LocalDate date){
        toEventList(getActivity(), date);
    }

    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> notifications) {
        this.notifications.clear();
        this.notifications.putAll(notifications);
        onCalendarUpdate(currentMonth);
    }

    private String getMonth(int month) {
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
    private void toEventList(@Nullable Activity activity, LocalDate localDate) {
        if(localDate == null) return;
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarFragment);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppValues.argDate, localDate);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEventsList, bundle);
            });
    }
}