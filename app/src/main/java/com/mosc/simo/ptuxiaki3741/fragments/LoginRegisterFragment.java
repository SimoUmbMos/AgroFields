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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders.LoginViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.User;

public class LoginRegisterFragment extends Fragment implements FragmentBackPress {
    public static final String TAG = "LoginRegisterFragment";
    private LoginViewHolder viewHolder;
    private UserViewModel vmUsers;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(getActivity() != null){
            if(getActivity() instanceof MainActivity){
                MainActivity mainActivity = (MainActivity) getActivity();
            }
        }
        return inflater.inflate(R.layout.fragment_login_register, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivity();
        init(view);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onBackPressed() {
        if(viewHolder.isRegisterMode()){
            viewHolder.showLogin();
            return false;
        }
        return true;
    }

    private void init(View view) {
        if(getActivity() != null){
            vmUsers = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            vmUsers.getCurrUser().observe(getViewLifecycleOwner(),this::onUserUpdate);
            onUserUpdate(vmUsers.getCurrUser().getValue());
        }
        viewHolder = new LoginViewHolder(
                getResources(),
                view,
                this::onLogin,
                this::onRegister,
                this::toRegister,
                this::toLogin
        );

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

    private void onUserUpdate(User user) {
        if(user != null){
            Log.d(TAG, "onUserUpdate: user not null");
            navigate(toMenu());
        }else{
            Log.d(TAG, "onUserUpdate: user null");
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
        viewHolder.clear();
        viewHolder.clearErrors();
        viewHolder.showRegister();
    }
    private void toLogin(View view) {
        viewHolder.clear();
        viewHolder.clearErrors();
        viewHolder.showLogin();
    }

    private User getLoginDataIfValid() {
        String  username = "",
                password = "";
        try{
            username = viewHolder.etUserName.getText().toString().trim();
            password = viewHolder.etMainPassword.getText().toString().trim();
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
            username = viewHolder.etUserName.getText().toString().trim();
            phone = viewHolder.etPhone.getText().toString().trim();
            email = viewHolder.etMainEmail.getText().toString().trim();
            email2 = viewHolder.etSecondaryEmail.getText().toString().trim();
            password = viewHolder.etMainPassword.getText().toString().trim();
            password2 =viewHolder.etSecondaryPassword.getText().toString().trim();
        }catch (NullPointerException e){
            Log.e(TAG, "getRegisterDataIfValid: ", e);
        }

        boolean isEmailSame = email.equals(email2),
                isPasswordSame = password.equals(password2),
                isPhoneLengthRight =
                        viewHolder.etPhone.length() == viewHolder.etPhoneLayout.getCounterMaxLength() ||
                        viewHolder.etPhone.length() == 0,
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
        User user = vmUsers.checkCredentials(tempUser.getUsername(),tempUser.getPassword());
        if (user != null) {
            vmUsers.singIn(user);
        }else{
            if(getActivity() != null)
                getActivity().runOnUiThread(()->{
                    Toast.makeText(
                            getContext(),
                            R.string.login_invalid_data_error,
                            Toast.LENGTH_SHORT
                    ).show();
                    viewHolder.showError(LoginRegisterError.UserNameWrongError);
                    viewHolder.showError(LoginRegisterError.PasswordWrongError);
                });
        }
    }
    private void onRegisterAction(User tempUser){
        User user = vmUsers.saveNewUser(tempUser);
        if(user != null){
            vmUsers.singIn(user);
        }else{
            if(getActivity() != null)
                getActivity().runOnUiThread(()->{
                        Toast.makeText(
                                getContext(),
                                R.string.register_invalid_data_error,
                                Toast.LENGTH_SHORT
                        ).show();
                        viewHolder.showError(LoginRegisterError.UserNameTakenError);
                });
        }
    }

    private void showLoginError(boolean isUsernameWritten, boolean isPasswordWritten) {
        viewHolder.clearErrors();
        if(!isUsernameWritten){
            viewHolder.showError(LoginRegisterError.UserNameEmptyError);
        }
        if(!isPasswordWritten){
            viewHolder.showError(LoginRegisterError.PasswordEmptyError);
        }
    }
    private void showRegisterError(boolean isEmailSame, boolean isEmailWritten,
                        boolean isPasswordSame, boolean isPasswordWritten,
                         boolean isUsernameWritten, boolean isPhoneLengthRight) {
        viewHolder.clearErrors();

        if(!isEmailWritten){
            viewHolder.showError(LoginRegisterError.EmailEmptyError);
        }else if(!isEmailSame){
            viewHolder.showError(LoginRegisterError.EmailNotMatchError);
        }

        if(!isPasswordWritten){
            viewHolder.showError(LoginRegisterError.PasswordEmptyError);
        }else if(!isPasswordSame){
            viewHolder.showError(LoginRegisterError.PasswordNotMatchError);
        }

        if(!isUsernameWritten){
            viewHolder.showError(LoginRegisterError.UserNameEmptyError);
        }

        if(!isPhoneLengthRight){
            viewHolder.showError(LoginRegisterError.PhoneLengthError);
        }
    }

    private void navigate(NavDirections action){
        NavController navController = NavHostFragment.findNavController(this);
        if( navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() == R.id.loginRegisterFragment)
            navController.navigate(action);
    }
    private NavDirections toMenu(){
        return  LoginRegisterFragmentDirections.toMenu();
    }
}