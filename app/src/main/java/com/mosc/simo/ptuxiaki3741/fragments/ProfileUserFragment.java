package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.util.UserUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentUserProfileBinding;import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

public class ProfileUserFragment extends Fragment implements FragmentBackPress {
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
        initMenu(menu);
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
        binding.btnUserProfileModify.setOnClickListener(v->onModifyClick());
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
    private void initMenu(Menu menu){
        MenuItem logout = menu.findItem(R.id.menu_item_logout);
        logout.getActionView().setOnClickListener(
                v-> menu.performIdentifierAction(logout.getItemId(),0)
        );
    }

    private void setupUiForUser(User user) {
        currUser = user;
        if(user != null){
            binding.etUserProfilePhone.setText(user.getPhone());
            binding.etUserProfileEmail.setText(user.getEmail());
        }else{
            toLogin(getActivity());
        }
    }

    private void onModifyClick() {
        setEditMode(!isEditMode);
        if(isEditMode){
            binding.btnUserProfileModify.setText(R.string.save_user);
        }else{
            binding.btnUserProfileModify.setText(R.string.edit_user);
            if(currUser != null){
                String email = getEmailData(),
                        phone = getPhoneData();
                User copy = new User(currUser.getUsername(),email,phone);
                LoginRegisterError result = UserUtil.checkUserData(copy);
                switch (result){
                    case EmailEmptyError:
                        binding.etUserProfileEmailLayout.setError(
                                getResources().getString(R.string.register_email_empty_error));
                        break;
                    case EmailInvalidCharacterError:
                        binding.etUserProfileEmailLayout.setError(
                                getResources().getString(R.string.register_email_invalid_error));
                        break;
                    case PhoneInvalidError:
                        binding.etUserProfilePhoneLayout.setError(
                                getResources().getString(R.string.register_phone_invalid_error));
                        break;
                    default:
                        binding.etUserProfileEmailLayout.setError(null);
                        binding.etUserProfilePhoneLayout.setError(null);
                        break;
                }
                if(result != LoginRegisterError.NONE){
                    setupUiForUser(currUser);
                }else{
                    currUser.setEmail(email);
                    currUser.setPhone(phone);
                    AsyncTask.execute(()->vmUsers.editUser(currUser));
                }
            }
        }
    }
    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        binding.etUserProfileEmail.setEnabled(isEditMode);
        binding.etUserProfilePhone.setEnabled(isEditMode);
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

    public void toLogin(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileUserFragment);
                if(nav != null)
                    nav.navigate(R.id.toLoginRegister);
            });
    }
}