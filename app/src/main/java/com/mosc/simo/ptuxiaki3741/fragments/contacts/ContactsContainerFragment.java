package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.badge.BadgeDrawable;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentContactsContainerBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.List;

public class ContactsContainerFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "ContactsContainerFragment";
    private FragmentContactsContainerBinding binding;
    private NavController navController;
    private int prevResId;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactsContainerBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initFragment();
        initViewHolder();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public boolean onBackPressed() {
        if(prevResId != R.id.MenuContactsFragment){
            binding.bottomNavigation.setSelectedItemId(R.id.bottom_menu_item_contacts_list);
            return false;
        }
        return true;
    }

    private void initData() {
        prevResId = -1;
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(binding.nhfContacts.getId());
        if(navHostFragment!=null){
            navController = navHostFragment.getNavController();
        }
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
            }
        }
    }
    private void initFragment() {
        MenuItem item = binding.bottomNavigation.getMenu().findItem(R.id.bottom_menu_item_hidden);
        if(item != null){
            item.setVisible(false);
        }
        binding.bottomNavigation.setOnItemSelectedListener(this::bottomNavListener);
        binding.fab.setOnClickListener(this::fabClickListener);
        binding.bottomNavigation.setSelectedItemId(R.id.bottom_menu_item_contacts_list);
    }
    private void initViewHolder(){
        if(getActivity() != null){
            UserViewModel vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getReceivedRequestList().observe(getViewLifecycleOwner(),this::onDataRefresh);
        }
    }

    private void onDataRefresh(List<User> receivedRequests) {
        MenuItem request = binding.bottomNavigation.getMenu()
                .findItem(R.id.bottom_menu_item_contacts_requests);
        if(request != null){
            if(receivedRequests != null){
                if(receivedRequests.size()>0){
                    BadgeDrawable badge = binding.bottomNavigation
                            .getOrCreateBadge(request.getItemId());
                    badge.setNumber(receivedRequests.size());
                }else{
                    binding.bottomNavigation.removeBadge(request.getItemId());
                }
            }else{
                binding.bottomNavigation.removeBadge(request.getItemId());
            }
        }
    }

    private void fabClickListener(View view) {
        binding.bottomNavigation.setSelectedItemId(R.id.bottom_menu_item_hidden);
    }
    private boolean bottomNavListener(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case (R.id.bottom_menu_item_contacts_list):
                navigateTo(R.id.MenuContactsFragment);
                return true;
            case (R.id.bottom_menu_item_contacts_requests):
                navigateTo(R.id.UserRequestFragment);
                return true;
            case (R.id.bottom_menu_item_hidden):
                navigateTo(R.id.searchContactFragment);
                return true;
            default:
                return false;
        }
    }

    private void navigateTo(int resID) {
        if(navController != null){
            if(prevResId == -1 || prevResId != resID){
                prevResId = resID;
                navController.navigate(resID);
                ColorStateList fabColor = null;
                if(getContext() != null){
                    if (resID == R.id.searchContactFragment) {
                        fabColor = ColorStateList.valueOf(
                                ContextCompat.getColor(getContext(), R.color.accentColor)
                        );
                    }else{
                        fabColor = ColorStateList.valueOf(
                                ContextCompat.getColor(getContext(), R.color.textColor)
                        );
                    }
                }
                if(fabColor != null){
                    binding.fab.setBackgroundTintList(fabColor);
                }
            }
        }
    }

}