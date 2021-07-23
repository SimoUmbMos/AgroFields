package com.mosc.simo.ptuxiaki3741.fragmentrelated.holders;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mosc.simo.ptuxiaki3741.R;

public class LoginViewHolder {
    public View root;
    public TextView tvLoginLabel, tvToRegister;
    public EditText etUserNameEmailPhone, etUserName,
            etMainPassword, etSecondaryPassword,
            etPhone, etMainEmail, etSecondaryEmail;
    public Button btnLoginCancel, btnLoginSubmit, btnLoginButton;
    private boolean isRegister;

    public LoginViewHolder(View view,
                           View.OnClickListener loginClick,
                           View.OnClickListener submitClick,
                           View.OnClickListener registerClick,
                           View.OnClickListener cancelClick){
        root = view;

        tvLoginLabel = view.findViewById(R.id.tvLoginLabel);
        tvToRegister = view.findViewById(R.id.tvToRegister);
        tvToRegister.setOnClickListener(registerClick);

        etUserNameEmailPhone = view.findViewById(R.id.etUserNameEmailPhone);
        etUserName = view.findViewById(R.id.etUserName);
        etMainPassword = view.findViewById(R.id.etMainPassword);
        etSecondaryPassword = view.findViewById(R.id.etSecondaryPassword);
        etPhone = view.findViewById(R.id.etPhone);
        etMainEmail = view.findViewById(R.id.etMainEmail);
        etSecondaryEmail = view.findViewById(R.id.etSecondaryEmail);

        btnLoginCancel = view.findViewById(R.id.btnLoginCancel);
        btnLoginSubmit = view.findViewById(R.id.btnLoginSubmit);
        btnLoginButton = view.findViewById(R.id.btnLoginButton);
        btnLoginButton.setOnClickListener(loginClick);
        btnLoginSubmit.setOnClickListener(submitClick);
        btnLoginCancel.setOnClickListener(cancelClick);

        showLogin();
    }

    public void showLogin(){
        isRegister = false;
        tvLoginLabel.setText(R.string.login_label);

        tvToRegister.setVisibility(View.VISIBLE);
        etUserNameEmailPhone.setVisibility(View.VISIBLE);
        btnLoginButton.setVisibility(View.VISIBLE);
        etMainPassword.setVisibility(View.VISIBLE);

        etPhone.setVisibility(View.GONE);
        etUserName.setVisibility(View.GONE);
        etMainEmail.setVisibility(View.GONE);
        btnLoginSubmit.setVisibility(View.GONE);
        btnLoginCancel.setVisibility(View.GONE);
        etSecondaryEmail.setVisibility(View.GONE);
        etSecondaryPassword.setVisibility(View.GONE);
    }
    public void showRegister(){
        isRegister = true;
        tvLoginLabel.setText(R.string.register_label);

        etPhone.setVisibility(View.VISIBLE);
        etUserName.setVisibility(View.VISIBLE);
        etMainEmail.setVisibility(View.VISIBLE);
        btnLoginSubmit.setVisibility(View.VISIBLE);
        btnLoginCancel.setVisibility(View.VISIBLE);
        etMainPassword.setVisibility(View.VISIBLE);
        etSecondaryEmail.setVisibility(View.VISIBLE);
        etSecondaryPassword.setVisibility(View.VISIBLE);

        tvToRegister.setVisibility(View.GONE);
        etUserNameEmailPhone.setVisibility(View.GONE);
        btnLoginButton.setVisibility(View.GONE);
    }
    public void clear(){
        etPhone.setText("");
        etUserName.setText("");
        etMainEmail.setText("");
        etMainPassword.setText("");
        etSecondaryEmail.setText("");
        etSecondaryPassword.setText("");
        etUserNameEmailPhone.setText("");
    }

    public boolean isRegisterMode(){
        return isRegister;
    }
}
