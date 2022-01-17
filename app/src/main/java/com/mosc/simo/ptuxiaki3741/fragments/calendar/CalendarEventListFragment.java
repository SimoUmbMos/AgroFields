package com.mosc.simo.ptuxiaki3741.fragments.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapters.NotificationsAdapter;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentCalendarEventListBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CalendarEventListFragment extends Fragment implements FragmentBackPress {
    private FragmentCalendarEventListBinding binding;
    private NotificationsAdapter adapter;
    private List<CalendarNotification> notifications;
    private LocalDate date;
    private boolean firstStart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentCalendarEventListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(initData()){
            initActivity();
            initFragment();
            initViewModel();
        }else{
            goBack();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private boolean initData(){
        firstStart = true;
        notifications = new ArrayList<>();
        date = null;
        if(getArguments() != null){
            if(getArguments().containsKey(AppValues.argDate)){
                date = (LocalDate) getArguments().getSerializable(AppValues.argDate);
            }
        }
        return date != null;
    }
    private void initActivity(){
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setOnBackPressed(this);
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if(actionBar != null){
                    StringBuilder builder = new StringBuilder();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, date.getYear());
                    calendar.set(Calendar.DAY_OF_YEAR, date.getDayOfYear());
                    builder.append(getString(R.string.calendar_event_list_fragment_title_1));
                    builder.append(" ");
                    builder.append(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date));
                    builder.append(" ");
                    builder.append(getString(R.string.calendar_event_list_fragment_title_2));
                    actionBar.setTitle(builder.toString());
                    actionBar.show();
                }
            }
        }
    }
    private void initFragment(){
        binding.tvNotificationsDisplay.setText(getString(R.string.loading_list));
        adapter = new NotificationsAdapter(notifications, this::onNotificationClick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        binding.rvNotificationsList.setHasFixedSize(true);
        binding.rvNotificationsList.setLayoutManager(layoutManager);
        binding.rvNotificationsList.setAdapter(adapter);

        binding.fabNewEvent.setOnClickListener(v->toNewEvent(getActivity()));
    }
    private void initViewModel(){
        if(getActivity() != null){
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getNotifications().observe(getViewLifecycleOwner(),this::onNotificationsUpdate);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onNotificationsUpdate(Map<LocalDate, List<CalendarNotification>> notifications) {
        this.notifications.clear();
        List<CalendarNotification> temp = notifications.getOrDefault(date,null);
        if(temp != null) {
            this.notifications.addAll(temp);
        }
        adapter.notifyDataSetChanged();
        onUpdateUi();
    }
    private void onNotificationClick(CalendarNotification notification) {
        toEvent(getActivity(), notification);
    }
    private void onUpdateUi() {
        if(notifications.size() > 0){
            binding.tvNotificationsDisplay.setVisibility(View.GONE);
            binding.rvNotificationsList.setVisibility(View.VISIBLE);
        }else{
            binding.tvNotificationsDisplay.setVisibility(View.VISIBLE);
            binding.rvNotificationsList.setVisibility(View.GONE);
        }
        if(firstStart){
            firstStart = false;
            binding.tvNotificationsDisplay.setText(getString(R.string.empty_list));
        }
    }

    private void goBack(){
        if(getActivity() != null) getActivity().onBackPressed();
    }
    private void toEvent(@Nullable Activity activity, CalendarNotification notification) {
        if(notification == null) return;
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppValues.argNotification, notification);
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }
    private void toNewEvent(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.CalendarEventListFragment);
                Bundle bundle = new Bundle();
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                bundle.putParcelable(
                        AppValues.argNotification,
                        new CalendarNotification(
                                0,
                                null,
                                null,
                                "",
                                "",
                                calendar.getTime()
                        )
                );
                if(nav != null)
                    nav.navigate(R.id.toCalendarEvent, bundle);
            });
    }
}