package com.mosc.simo.ptuxiaki3741.fragments.user;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
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
    private ActionBar actionBar;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModels();
        initFragment();
        initObservers();
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
            onLogoutClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        if(isEditMode){
            onResetClick();
            return false;
        }
        return true;
    }

    private void initActivity(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if( mainActivity != null){
            mainActivity.setOnBackPressed(this);
            actionBar = mainActivity.getSupportActionBar();
            if(actionBar != null){
                actionBar.show();
            }
        }
    }
    private void initViewModels(){
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }
    private void initFragment(){
        toNormalMode();
    }
    private void initObservers(){
        if(vmUsers != null){
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
        }else{
            onUserUpdate(null);
        }
    }
    private void initMenu(Menu menu){
        MenuItem logout = menu.findItem(R.id.menu_item_logout);
        logout.getActionView().setOnClickListener(
                v-> menu.performIdentifierAction(logout.getItemId(),0)
        );
    }

    private void clearErrors(){
        binding.etUserProfileEmailLayout.setError(null);
        binding.etUserProfilePhoneLayout.setError(null);
    }

    private void onUserUpdate(User user) {
        currUser = user;
        if(user != null){
            binding.etUserProfilePhone.setText(user.getPhone());
            binding.etUserProfileEmail.setText(user.getEmail());
        }else{
            toLoading(getActivity());
        }
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

    private void onEditClick(){
        toEditMode();
    }
    private void onSaveClick(){
        if(saveAction())
            toNormalMode();
    }
    private void onLogoutClick(){
        vmUsers.logout();
    }
    private void onResetClick(){
        if(isEditMode){
            clearErrors();
            onUserUpdate(currUser);
            toNormalMode();
        }
    }

    private boolean saveAction(){
        clearErrors();
        if(currUser != null){
            String email = getEmailData(), phone = getPhoneData();
            User copy = new User(currUser.getUsername(),phone,email);
            LoginRegisterError result = UserUtil.checkUserData(copy);
            if(result == LoginRegisterError.NONE){
                currUser.setEmail(email);
                currUser.setPhone(phone);
                AsyncTask.execute(()->vmUsers.editUser(currUser));
                return true;
            }else{
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
                }
            }
        }
        return false;
    }

    private void toEditMode(){
        isEditMode = true;
        clearErrors();
        binding.etUserProfileEmail.setEnabled(true);
        binding.etUserProfilePhone.setEnabled(true);
        binding.btnUserProfileModify.setText(R.string.save_user);
        binding.btnUserProfileModify.setOnClickListener(v->onSaveClick());
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.profile_edit_bar_label));
        }
        if(getContext() != null){
            binding.btnUserProfileModify.setBackgroundTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    getContext(),
                                    R.color.accentColor
                            )
                    )
            );
            binding.btnUserProfileModify.setTextColor(
                    ContextCompat.getColor(
                            getContext(),
                            R.color.backgroundColor
                    )
            );
        }
    }
    private void toNormalMode(){
        isEditMode = false;
        clearErrors();
        binding.etUserProfileEmail.setEnabled(false);
        binding.etUserProfilePhone.setEnabled(false);
        binding.btnUserProfileModify.setText(R.string.edit_user);
        binding.btnUserProfileModify.setOnClickListener(v->onEditClick());
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.profile_bar_label));
        }
        if(getContext() != null){
            binding.btnUserProfileModify.setBackgroundTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    getContext(),
                                    R.color.cardBackgroundColor
                            )
                    )
            );
            binding.btnUserProfileModify.setTextColor(
                    ContextCompat.getColor(
                            getContext(),
                            R.color.textColor
                    )
            );
        }
    }
    private void toLoading(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.ProfileUserFragment);
                if(nav != null)
                    nav.navigate(R.id.toLoading);
            });
    }
}