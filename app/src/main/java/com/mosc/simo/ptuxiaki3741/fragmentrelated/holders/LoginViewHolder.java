package com.mosc.simo.ptuxiaki3741.fragmentrelated.holders;

import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;

public class LoginViewHolder {
    private Resources res;
    public View root;
    public TextView tvLoginLabel, tvToRegister;
    public EditText  etUserName,
            etMainPassword, etSecondaryPassword,
            etPhone, etMainEmail, etSecondaryEmail;
    public Button btnLoginCancel, btnLoginSubmit, btnLoginButton;
    private boolean isRegister;

    public LoginViewHolder(Resources res,
                           View view,
                           View.OnClickListener loginClick,
                           View.OnClickListener submitClick,
                           View.OnClickListener registerClick,
                           View.OnClickListener cancelClick){

        this.res = res;
        root = view;

        tvLoginLabel = view.findViewById(R.id.tvLoginLabel);
        tvToRegister = view.findViewById(R.id.tvToRegister);
        tvToRegister.setOnClickListener(registerClick);

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
        btnLoginButton.setVisibility(View.VISIBLE);
        etUserName.setVisibility(View.VISIBLE);
        etMainPassword.setVisibility(View.VISIBLE);

        etPhone.setVisibility(View.GONE);
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
        btnLoginButton.setVisibility(View.GONE);
    }
    public void clear(){
        etPhone.setText("");
        etUserName.setText("");
        etMainEmail.setText("");
        etMainPassword.setText("");
        etSecondaryEmail.setText("");
        etSecondaryPassword.setText("");
    }

    public boolean isRegisterMode(){
        return isRegister;
    }

    public void clearErrors() {
        etPhone.setError(null);
        etUserName.setError(null);
        etMainEmail.setError(null);
        etMainPassword.setError(null);
        etSecondaryEmail.setError(null);
        etSecondaryPassword.setError(null);
    }
    public void showError(LoginRegisterError error) {
        switch (error){
            case EmailEmptyError:
                etMainEmail.setError(res.getString(R.string.register_email_empty_error));
                break;
            case EmailNotMatchError:
                etMainEmail.setError(res.getString(R.string.register_email_not_match_error));
                etSecondaryEmail.setError(res.getString(R.string.register_email_not_match_error));
                break;
            case PasswordWrongError:
                etMainPassword.setError(res.getString(R.string.register_password_wrong_error));
                break;
            case PasswordEmptyError:
                etMainPassword.setError(res.getString(R.string.register_password_empty_error));
                break;
            case PasswordNotMatchError:
                etMainPassword.setError(res.getString(R.string.register_password_not_match_error));
                etSecondaryPassword.setError(res.getString(R.string.register_password_not_match_error));
                break;
            case PhoneEmptyError:
                etPhone.setError(res.getString(R.string.register_phone_empty_error));
                break;
            case UserNameWrongError:
                etUserName.setError(res.getString(R.string.register_username_wrong_error));
                break;
            case UserNameEmptyError:
                etUserName.setError(res.getString(R.string.register_username_empty_error));
                break;
            case UserNameTakenError:
                etUserName.setError(res.getString(R.string.register_username_taken_error));
                break;
        }
    }
}
