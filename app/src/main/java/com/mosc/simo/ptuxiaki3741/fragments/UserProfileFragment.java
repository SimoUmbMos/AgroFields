package com.mosc.simo.ptuxiaki3741.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.UserProfileViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

public class UserProfileFragment extends Fragment implements FragmentBackPress {
    private UserProfileViewHolder viewHolder;
    private User currUser;
    private UserViewModel vmUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initFragment(view);
        initViewModels();
    }
    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void initActivity(){
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
    private void initFragment(View view){
        viewHolder = new UserProfileViewHolder(view,this::onModifyClick);
        setupUiForUser(null);
    }

    private void initViewModels(){
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::setupUiForUser);
        }
    }

    private void setupUiForUser(User user) {
        currUser = user;
        if(user != null){
            String title = getResources().getString(R.string.profile_title_1) + " " +
                    user.getUsername() + " " + getResources().getString(R.string.profile_title_2);
            viewHolder.tvTitle.setText(title);
            viewHolder.etPhone.setText(user.getPhone());
            viewHolder.etEmail.setText(user.getEmail());
        }else{
            viewHolder.tvTitle.setText("");
            viewHolder.etPhone.setText("");
            viewHolder.etEmail.setText("");
        }
    }

    private void onModifyClick(View view) {
        viewHolder.setEditMode(!viewHolder.isEditMode());
        if(viewHolder.isEditMode()){
            viewHolder.btnModify.setText(R.string.save);
        }else{
            viewHolder.btnModify.setText(R.string.edit);
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

    private boolean isDataValid(){
        if(
                viewHolder.etEmail.getText() != null &&
                viewHolder.etPhone.getText() != null
        ){
            String email = viewHolder.etEmail.getText().toString();
            return !email.trim().isEmpty();
        }
        return false;
    }
    private String getEmailData(){
        if(viewHolder.etEmail.getText() != null){
            String email = viewHolder.etEmail.getText().toString();
            return email.trim();
        }
        return "";
    }
    private String getPhoneData(){
        if(viewHolder.etPhone.getText() != null){
            String phone = viewHolder.etPhone.getText().toString();
            return phone.trim();
        }
        return "";
    }
}