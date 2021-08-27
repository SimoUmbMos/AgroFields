package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserProfileBinding;import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

public class UserProfileFragment extends Fragment implements FragmentBackPress {
    private FragmentUserProfileBinding binding;
    private User currUser;
    private boolean isEditMode;
    private UserViewModel vmUsers;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment();
        initViewModels();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_item_logout){
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(getString(R.string.profile_bar_label));
                actionBar.show();
            }
        }
    }
    private void initFragment(){
        binding.btnUserProfileModify.setOnClickListener(this::onModifyClick);
        setEditMode(false);
    }
    private void initViewModels(){
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::setupUiForUser);
        }else{
            setupUiForUser(null);
        }
    }

    private void setupUiForUser(User user) {
        currUser = user;
        if(user != null){
            binding.etUserProfilePhone.setText(user.getPhone());
            binding.etUserProfileEmail.setText(user.getEmail());
        }else{
            navigate(toLogin());
        }
    }

    private void onModifyClick(View view) {
        setEditMode(!isEditMode);
        if(isEditMode){
            binding.btnUserProfileModify.setText(R.string.save);
        }else{
            binding.btnUserProfileModify.setText(R.string.edit);
            if(currUser != null){
                if(isDataValid()){
                    currUser.setEmail(getEmailData());
                    currUser.setPhone(getPhoneData());
                    AsyncTask.execute(()->vmUsers.editUser(currUser));
                }else{
                    setupUiForUser(currUser);
                }
            }
        }
    }
    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        binding.etUserProfileEmail.setEnabled(isEditMode);
        binding.etUserProfilePhone.setEnabled(isEditMode);
    }

    private boolean isDataValid(){
        if(
                binding.etUserProfileEmail.getText() != null
        ){
            String email = binding.etUserProfileEmail.getText().toString();
            return !email.trim().isEmpty();
        }
        return false;
    }
    private String getEmailData(){
        if(binding.etUserProfileEmail.getText() != null){
            String email = binding.etUserProfileEmail.getText().toString();
            return email.trim();
        }
        return "";
    }
    private String getPhoneData(){
        if(binding.etUserProfilePhone.getText() != null){
            String phone = binding.etUserProfilePhone.getText().toString();
            return phone.trim();
        }
        return "";
    }

    private void logout(){
        vmUsers.logout();
    }

    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if(action != null){
            if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.UserProfileFragment)
                navController.navigate(action);
        }
    }
    public NavDirections toLogin() {
        return UserProfileFragmentDirections.toLogin();
    }
}