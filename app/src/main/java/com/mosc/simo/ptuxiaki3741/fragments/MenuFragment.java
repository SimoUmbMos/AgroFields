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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentMenuMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

public class MenuFragment extends Fragment implements FragmentBackPress {
    public static final String TAG ="MenuFragment";

    private UserViewModel vmUsers;
    private NavController navController;
    private FragmentMenuMainBinding binding;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMenuMainBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModels();
        initObservers();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public boolean onBackPressed() {
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
    private void initFragment() {
        navController = NavHostFragment.findNavController(this);

        binding.btnMainMenuList.setOnClickListener(v -> toListMenu(getActivity()));
        binding.btnMainMenuHistory.setOnClickListener(v -> toLandHistory(getActivity()));

        binding.btnMainMenuFriends.setOnClickListener(v -> Toast.makeText(getContext(),"TODO",Toast.LENGTH_SHORT).show()); //TODO: menu actions

        binding.btnMainMenuProfile.setOnClickListener(v -> toProfile(getActivity()));
        binding.btnMainMenuLogout.setOnClickListener(v -> vmUsers.logout());
    }
    private void initViewModels() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initObservers() {
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onCurrUserUpdate);
        }
    }

    private void onCurrUserUpdate(User user) {
        if(user == null){
            toLogin(getActivity());
        }
    }

    private void navigate(NavDirections action){
        if(action != null){
            if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.MainMenuFragment)
                navController.navigate(action);
        }
    }
    public void toLandHistory(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toLandHistory();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
    public void toListMenu(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toListMenu();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
    public void toProfile(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toUserProfile();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
    public void toLogin(@Nullable Activity activity) {
        NavDirections action = MenuFragmentDirections.toLogin();
        if(activity != null)
            activity.runOnUiThread(()->navigate(action));
    }
}