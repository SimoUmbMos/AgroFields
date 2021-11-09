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
import com.mosc.simo.ptuxiaki3741.util.UserUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentLoginRegisterBinding;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

public class LoginRegisterFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LoginRegisterFragment";
    private FragmentLoginRegisterBinding binding;
    private UserViewModel vmUsers;
    private boolean isRegister;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentLoginRegisterBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        initViewModel();
        init();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onBackPressed() {
        if(isRegister){
            showLogin();
            return false;
        }
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
    private void initViewModel() {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
        }
    }
    private void init() {
        showLogin();
        binding.btnLoginSubmit.setOnClickListener(this::onSubmit);
        binding.tvLoginSwitch.setOnClickListener(this::onSwitchUI);
    }

    private void onSubmit(View view) {
        if(isRegister){
            onRegister();
        }else{
            onLogin();
        }
    }
    private void onSwitchUI(View view) {
        if(isRegister){
            toLogin();
        }else{
            toRegister();
        }
    }

    private void onUserUpdate(User user) {
        if(user != null){
            toMenu(getActivity());
        }
    }

    private void onLogin() {
        User user = getLoginDataIfValid();
        if(user != null){
            AsyncTask.execute(()->onLoginAction(user));
        }
    }
    private void onRegister() {
        User user = getRegisterDataIfValid();
        if(user != null){
            startLoadingAnimation();
            AsyncTask.execute(()->onRegisterAction(user));
        }
    }
    private void onLoginAction(User tempUser){
        User user = vmUsers.checkCredentials(tempUser);
        if (user != null) {
            vmUsers.singIn(user);
        }else{
            if(getActivity() != null) {
                boolean isValidUsername = vmUsers.checkUserNameCredentials(tempUser) != null;
                getActivity().runOnUiThread(() -> {
                    if (isValidUsername) {
                        showError(LoginRegisterError.PasswordWrongError);
                    } else {
                        showError(LoginRegisterError.UserNameWrongError);
                    }
                });
            }
        }
    }
    private void onRegisterAction(User tempUser){
        if(getActivity() != null)
            getActivity().runOnUiThread(this::stopLoadingAnimation);
        User user = vmUsers.saveNewUser(tempUser);
        if(user != null){
            vmUsers.singIn(user);
        }else{
            LoginRegisterError error = vmUsers.getNewUserError(tempUser);
            if(getActivity() != null)
                getActivity().runOnUiThread(()-> showError(error));
        }
    }
    private void toRegister() {
        clearFields();
        showError(LoginRegisterError.NONE);
        showRegister();
    }
    private void toLogin() {
        clearFields();
        showError(LoginRegisterError.NONE);
        showLogin();
    }

    private User getLoginDataIfValid() {
        showError(LoginRegisterError.NONE);
        String  username = "", password = "";
        if(binding.etLoginUserName.getText() != null){
            username = binding.etLoginUserName.getText().toString().trim();
        }
        if(binding.etLoginMainPassword.getText() != null){
            password = binding.etLoginMainPassword.getText().toString().trim();
        }

        LoginRegisterError error = UserUtil.checkData(username, password);

        if(error == LoginRegisterError.NONE){
            return new User(username, password);
        }else{
            showError(error);
        }
        return null;
    }
    private User getRegisterDataIfValid() {
        showError(LoginRegisterError.NONE);
        String  username = "",
                phone = "",
                email = "",
                password = "",
                password2 = "";
        if(binding.etLoginUserName.getText() != null){
            username = binding.etLoginUserName.getText().toString().trim();
        }
        if(binding.etLoginPhone.getText() != null){
            phone = binding.etLoginPhone.getText().toString().trim();
        }
        if(binding.etLoginMainEmail.getText() != null){
            email = binding.etLoginMainEmail.getText().toString().trim();
        }
        if(binding.etLoginMainPassword.getText() != null){
            password = binding.etLoginMainPassword.getText().toString().trim();
        }
        if(binding.etLoginSecondaryPassword.getText() != null){
            password2 =binding.etLoginSecondaryPassword.getText().toString().trim();
        }
        LoginRegisterError error = UserUtil.checkData(username,password,password2,email,phone);
        if(error == LoginRegisterError.NONE){
            return new User(username, password, phone, email);
        }else{
            showError(error);
        }
        return null;
    }

    //ui
    public void showLogin(){
        isRegister = false;
        binding.tvLoginLabel.setText(R.string.login_label);

        binding.btnLoginSubmit.setText(getString(R.string.submit_login));
        binding.tvLoginSwitch.setText(getString(R.string.login_switch_btn));
        binding.tvLoginRegisterHint.setText(getString(R.string.login_hint));

        binding.etLoginPhoneLayout.setVisibility(View.GONE);
        binding.etLoginMainEmailLayout.setVisibility(View.GONE);
        binding.etLoginSecondaryPasswordLayout.setVisibility(View.GONE);
    }
    public void showRegister(){
        isRegister = true;
        binding.tvLoginLabel.setText(R.string.register_label);

        binding.btnLoginSubmit.setText(getString(R.string.submit_register));
        binding.tvLoginSwitch.setText(getString(R.string.register_switch_btn));
        binding.tvLoginRegisterHint.setText(getString(R.string.register_hint));

        binding.etLoginPhoneLayout.setVisibility(View.VISIBLE);
        binding.etLoginMainEmailLayout.setVisibility(View.VISIBLE);
        binding.etLoginSecondaryPasswordLayout.setVisibility(View.VISIBLE);

    }
    public void clearFields(){
        binding.etLoginPhone.setText("");
        binding.etLoginUserName.setText("");
        binding.etLoginMainEmail.setText("");
        binding.etLoginMainPassword.setText("");
        binding.etLoginSecondaryPassword.setText("");
    }
    public void showError(LoginRegisterError error) {
        switch (error){
            case UserNameEmptyError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_empty_error));
                break;
            case UserNameSizeError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_size_error));
                break;
            case UserNameInvalidCharacterError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_invalid_error));
                break;
            case UserNameTakenError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_taken_error));
                break;
            case UserNameWrongError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_wrong_error));
                break;

            case PasswordEmptyError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_empty_error));
                break;
            case PasswordSizeError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_size_error));
                break;
            case PasswordInvalidCharacterError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_invalid_error));
                break;
            case Password2EmptyError:
                binding.etLoginSecondaryPasswordLayout.setError(
                        getResources().getString(R.string.register_password_empty_error));
                break;
            case PasswordNotMatchError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_not_match_error));
                binding.etLoginSecondaryPasswordLayout.setError(
                        getResources().getString(R.string.register_password_not_match_error));
                break;
            case PasswordWrongError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_wrong_error));
                break;

            case EmailEmptyError:
                binding.etLoginMainEmailLayout.setError(
                        getResources().getString(R.string.register_email_empty_error));
                break;
            case EmailInvalidCharacterError:
                binding.etLoginMainEmailLayout.setError(
                        getResources().getString(R.string.register_email_invalid_error));
                break;
            case EmailTakenError:
                binding.etLoginMainEmailLayout.setError(
                        getResources().getString(R.string.register_email_taken_error));
                break;

            case PhoneInvalidError:
                binding.etLoginPhoneLayout.setError(
                        getResources().getString(R.string.register_phone_invalid_error));
                break;
            case NONE:
            default:
                binding.etLoginPhoneLayout.setError(null);
                binding.etLoginUserNameLayout.setError(null);
                binding.etLoginMainEmailLayout.setError(null);
                binding.etLoginMainPasswordLayout.setError(null);
                binding.etLoginSecondaryPasswordLayout.setError(null);
                break;
        }
    }
    public void startLoadingAnimation(){
        //fixme: code
    }
    public void stopLoadingAnimation(){
        //fixme: code
    }

    public void toMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoginRegisterFragment);
                if(nav != null)
                    nav.navigate(R.id.toMenuMain);
            });
    }
}