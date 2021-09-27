package com.mosc.simo.ptuxiaki3741.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
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
            onUserUpdate(vmUsers.getCurrUser().getValue());
        }
    }
    private void init() {
        binding.btnLoginButton.setOnClickListener(this::onLogin);
        binding.btnLoginSubmit.setOnClickListener(this::onRegister);
        binding.btnLoginRegister.setOnClickListener(this::toRegister);
        binding.btnLoginCancel.setOnClickListener(this::toLogin);
        showLogin();
    }

    private void onUserUpdate(User user) {
        if(user != null){
            toMenu(getActivity());
        }
    }

    private void onLogin(View view) {
        User user = getLoginDataIfValid();
        if(user != null){
            AsyncTask.execute(()->onLoginAction(user));
        }
    }
    private void onRegister(View view) {
        User user = getRegisterDataIfValid();
        if(user != null){
            AsyncTask.execute(()->onRegisterAction(user));
        }
    }
    private void toRegister(View view) {
        clear();
        clearErrors();
        showRegister();
    }
    private void toLogin(View view) {
        clear();
        clearErrors();
        showLogin();
    }

    private User getLoginDataIfValid() {
        String  username = "",
                password = "";
        try{
            if(
                    binding.etLoginUserName.getText() != null &&
                    binding.etLoginMainPassword.getText() != null
            ){
                username = binding.etLoginUserName.getText().toString().trim();
                password = binding.etLoginMainPassword.getText().toString().trim();
            }else{
                username = "";
                password = "";
            }
        }catch (NullPointerException e){
            Log.e(TAG, "getLoginDataIfValid: ", e);
        }

        boolean isPasswordWritten = !password.isEmpty(),
                isUsernameWritten = !username.isEmpty();

        showLoginError(isUsernameWritten, isPasswordWritten);

        if(isPasswordWritten && isUsernameWritten){
            return new User(username, password);
        }
        return null;
    }
    private User getRegisterDataIfValid() {
        String  username = "",
                phone = "",
                email = "",
                email2 = "",
                password = "",
                password2 = "";
        try{
            if(
                    binding.etLoginUserName.getText() != null &&
                    binding.etLoginPhone.getText() != null &&
                    binding.etLoginMainEmail.getText() != null &&
                    binding.etLoginSecondaryEmail.getText() != null &&
                    binding.etLoginMainPassword.getText() != null &&
                    binding.etLoginSecondaryPassword.getText() != null
            ){
                username = binding.etLoginUserName.getText().toString().trim();
                phone = binding.etLoginPhone.getText().toString().trim();
                email = binding.etLoginMainEmail.getText().toString().trim();
                email2 = binding.etLoginSecondaryEmail.getText().toString().trim();
                password = binding.etLoginMainPassword.getText().toString().trim();
                password2 =binding.etLoginSecondaryPassword.getText().toString().trim();
            }else{
                username = "";
                phone = "";
                email = "";
                email2 = "";
                password = "";
                password2 = "";
            }
        }catch (NullPointerException e){
            Log.e(TAG, "getRegisterDataIfValid: ", e);
        }

        boolean isEmailSame = email.equals(email2),
                isPasswordSame = password.equals(password2),
                isPhoneLengthRight =
                        binding.etLoginPhone.length() ==
                                binding.etLoginPhoneLayout.getCounterMaxLength() ||
                        binding.etLoginPhone.length() == 0,
                isEmailWritten = !email.isEmpty(),
                isPasswordWritten = !password.isEmpty(),
                isUsernameWritten = !username.isEmpty();

        showRegisterError(isEmailSame, isEmailWritten,
                isPasswordSame, isPasswordWritten,
                isUsernameWritten, isPhoneLengthRight);

        if(isEmailWritten && isEmailSame &&
                isPasswordWritten && isPasswordSame &&
                isPhoneLengthRight && isUsernameWritten){
            return new User(username, password, phone, email);
        }
        return null;
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
        User user = vmUsers.saveNewUser(tempUser);
        if(user != null){
            vmUsers.singIn(user);
        }else{
            if(getActivity() != null)
                getActivity().runOnUiThread(()->
                        showError(LoginRegisterError.UserNameTakenError)
                );
        }
    }

    private void showLoginError(boolean isUsernameWritten, boolean isPasswordWritten) {
        clearErrors();
        if(!isUsernameWritten){
            showError(LoginRegisterError.UserNameEmptyError);
        }
        if(!isPasswordWritten){
            showError(LoginRegisterError.PasswordEmptyError);
        }
    }
    private void showRegisterError(boolean isEmailSame, boolean isEmailWritten,
                        boolean isPasswordSame, boolean isPasswordWritten,
                         boolean isUsernameWritten, boolean isPhoneLengthRight) {
        clearErrors();

        if(!isEmailWritten){
            showError(LoginRegisterError.EmailEmptyError);
        }else if(!isEmailSame){
            showError(LoginRegisterError.EmailNotMatchError);
        }

        if(!isPasswordWritten){
            showError(LoginRegisterError.PasswordEmptyError);
        }else if(!isPasswordSame){
            showError(LoginRegisterError.PasswordNotMatchError);
        }

        if(!isUsernameWritten){
            showError(LoginRegisterError.UserNameEmptyError);
        }

        if(!isPhoneLengthRight){
            showError(LoginRegisterError.PhoneLengthError);
        }
    }

    //ui
    public void showLogin(){
        isRegister = false;
        binding.tvLoginLabel.setText(R.string.login_label);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                binding.llLoginButtons.getLayoutParams();
        params.verticalBias = 0.65f;
        binding.llLoginButtons.setLayoutParams(params);

        binding.btnLoginRegister.setVisibility(View.VISIBLE);
        binding.btnLoginButton.setVisibility(View.VISIBLE);
        binding.btnLoginSubmit.setVisibility(View.GONE);
        binding.btnLoginCancel.setVisibility(View.GONE);

        binding.etLoginPhoneLayout.setVisibility(View.GONE);
        binding.etLoginMainEmailLayout.setVisibility(View.GONE);
        binding.etLoginSecondaryEmailLayout.setVisibility(View.GONE);
        binding.etLoginSecondaryPasswordLayout.setVisibility(View.GONE);
    }
    public void showRegister(){
        isRegister = true;
        binding.tvLoginLabel.setText(R.string.register_label);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                binding.llLoginButtons.getLayoutParams();
        params.verticalBias = 1.0f;
        binding.llLoginButtons.setLayoutParams(params);

        binding.btnLoginRegister.setVisibility(View.GONE);
        binding.btnLoginButton.setVisibility(View.GONE);
        binding.btnLoginSubmit.setVisibility(View.VISIBLE);
        binding.btnLoginCancel.setVisibility(View.VISIBLE);

        binding.etLoginPhoneLayout.setVisibility(View.VISIBLE);
        binding.etLoginMainEmailLayout.setVisibility(View.VISIBLE);
        binding.etLoginSecondaryEmailLayout.setVisibility(View.VISIBLE);
        binding.etLoginSecondaryPasswordLayout.setVisibility(View.VISIBLE);

    }
    public void clear(){
        binding.etLoginPhone.setText("");
        binding.etLoginUserName.setText("");
        binding.etLoginMainEmail.setText("");
        binding.etLoginMainPassword.setText("");
        binding.etLoginSecondaryEmail.setText("");
        binding.etLoginSecondaryPassword.setText("");
    }
    public void clearErrors() {
        binding.etLoginPhoneLayout.setError(null);
        binding.etLoginUserNameLayout.setError(null);
        binding.etLoginMainEmailLayout.setError(null);
        binding.etLoginMainPasswordLayout.setError(null);
        binding.etLoginSecondaryEmailLayout.setError(null);
        binding.etLoginSecondaryPasswordLayout.setError(null);
    }
    public void showError(LoginRegisterError error) {
        switch (error){
            case EmailEmptyError:
                binding.etLoginMainEmailLayout.setError(
                        getResources().getString(R.string.register_email_empty_error));
                break;
            case EmailNotMatchError:
                binding.etLoginMainEmailLayout.setError(
                        getResources().getString(R.string.register_email_not_match_error));
                binding.etLoginSecondaryEmailLayout.setError(
                        getResources().getString(R.string.register_email_not_match_error));
                break;
            case PasswordWrongError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_wrong_error));
                break;
            case PasswordEmptyError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_empty_error));
                break;
            case PasswordNotMatchError:
                binding.etLoginMainPasswordLayout.setError(
                        getResources().getString(R.string.register_password_not_match_error));
                binding.etLoginSecondaryPasswordLayout.setError(
                        getResources().getString(R.string.register_password_not_match_error));
                break;
            case PhoneLengthError:
                binding.etLoginPhoneLayout.setError(
                        getResources().getString(R.string.register_phone_length_error));
                break;
            case UserNameWrongError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_wrong_error));
                break;
            case UserNameEmptyError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_empty_error));
                break;
            case UserNameTakenError:
                binding.etLoginUserNameLayout.setError(
                        getResources().getString(R.string.register_username_taken_error));
                break;
        }
    }

    public void toMenu(@Nullable Activity activity) {
        if(activity != null)
            activity.runOnUiThread(()-> {
                NavController nav = UIUtil.getNavController(this,R.id.LoginFragment);
                if(nav != null)
                    nav.navigate(R.id.loginToMenu);
            });
    }
}