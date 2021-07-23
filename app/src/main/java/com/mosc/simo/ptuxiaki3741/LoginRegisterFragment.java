package com.mosc.simo.ptuxiaki3741;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.holders.LoginViewHolder;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;

public class LoginRegisterFragment extends Fragment implements FragmentBackPress {
    private LoginViewHolder viewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_login_register, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        return true;
    }

    private void init(View view) {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null){
            activity.setOnBackPressed(this);
        }
        viewHolder = new LoginViewHolder(
                view,
                this::onLogin,
                this::onRegister,
                this::toRegister,
                this::toLogin
        );
    }

    private void onLogin(View view) {
        //TODO: REPLACE WITH REAL METHOD
        Snackbar.make(view,R.string.todo,Snackbar.LENGTH_LONG).show();
    }
    private void onRegister(View view) {
        if(registerDataIsValid()){
            //TODO: REPLACE WITH REAL METHOD
            Snackbar.make(view,R.string.todo,Snackbar.LENGTH_LONG).show();
        }else{
            Snackbar.make(view,R.string.register_invalid_data_error,Snackbar.LENGTH_LONG).show();
        }
    }
    private void toRegister(View view) {
        viewHolder.clear();
        viewHolder.showRegister();
    }
    private void toLogin(View view) {
        viewHolder.clear();
        viewHolder.showLogin();
    }

    private boolean registerDataIsValid() {
        String  username = viewHolder.etUserName.getText().toString().trim(),
                email = viewHolder.etMainEmail.getText().toString().trim(),
                email2 = viewHolder.etSecondaryEmail.getText().toString().trim(),
                password = viewHolder.etMainPassword.getText().toString().trim(),
                password2 =viewHolder.etSecondaryPassword.getText().toString().trim();

        boolean isEmailSame = email.equals(email2),
                isPasswordSame = password.equals(password2),
                isEmailWritten  = !email.isEmpty(),
                isPasswordWritten = !password.isEmpty(),
                isUsernameWritten = !username.isEmpty();

        return  isEmailSame && isPasswordSame &&
                isUsernameWritten && isEmailWritten && isPasswordWritten;
    }
}