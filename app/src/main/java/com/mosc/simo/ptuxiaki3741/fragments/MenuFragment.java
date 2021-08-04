package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.MainMenuHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public class MenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="MenuFragment";

    private LandViewModel vmLands;
    private UserViewModel vmUsers;
    private NavController navController;
    private MainMenuHolder menuHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment(view);
        initViewModels();
        initObservers();
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initActivity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        ActionBar actionBar = null;
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
        }
        if(actionBar != null){
            actionBar.setTitle("");
            actionBar.hide();
        }
    }
    private void initFragment(View view) {
        navController = NavHostFragment.findNavController(this);
        menuHolder = new MainMenuHolder(view);

        menuHolder.btnList.setOnClickListener(v -> toListMenu(getActivity()));
        menuHolder.btnLogout.setOnClickListener(v -> vmUsers.logout());
        //TODO: menu actions
        menuHolder.btnHistory.setOnClickListener(v -> Toast.makeText(getContext(),"TODO",Toast.LENGTH_SHORT).show());
        menuHolder.btnFriends.setOnClickListener(v -> Toast.makeText(getContext(),"TODO",Toast.LENGTH_SHORT).show());
        menuHolder.btnProfile.setOnClickListener(v -> Toast.makeText(getContext(),"TODO",Toast.LENGTH_SHORT).show());
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmLands = new ViewModelProvider(getActivity()).get(LandViewModel.class);
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initObservers() {
        if(vmUsers != null && vmLands != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
            vmLands.getLands().observe(getViewLifecycleOwner(),this::onLandListUpdate);
        }
    }

    private void onCurrUserUpdate(User user) {
        if(user == null){
            Log.d(TAG, "onUserUpdate: user null");
            toLogin(getActivity());
        }else{
            Log.d(TAG, "onUserUpdate: user not null");
        }
    }
    private void onLandListUpdate(List<Land> lands) {

    }


    private void navigate(NavDirections action){
        if(action != null){
            if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.menuFragment)
                navController.navigate(action);
        }
    }
    public void toListMenu(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toListMenu();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
    public void toLogin(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toLogin();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
}