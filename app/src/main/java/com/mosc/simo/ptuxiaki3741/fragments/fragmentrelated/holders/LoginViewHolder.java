package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;

public class LoginViewHolder {
    private final Resources res;
    public final View root;
    public final LinearLayout llLoginData, llLoginButtons;
    public final TextView tvLoginLabel;
    public final TextInputEditText etUserName,
            etMainPassword, etSecondaryPassword,
            etPhone, etMainEmail, etSecondaryEmail;
    public final TextInputLayout etUserNameLayout,
            etMainPasswordLayout,etSecondaryPasswordLayout,
            etPhoneLayout, etMainEmailLayout, etSecondaryEmailLayout;
    public final Button btnLoginCancel, btnLoginSubmit, btnToRegister, btnLoginButton;
    private boolean isRegister;

    public LoginViewHolder(Resources res,
                           View view,
                           View.OnClickListener loginClick,
                           View.OnClickListener submitClick,
                           View.OnClickListener registerClick,
                           View.OnClickListener cancelClick){

        this.res = res;
        root = view;

        llLoginData = view.findViewById(R.id.llLoginData);
        llLoginButtons = view.findViewById(R.id.llLoginButtons);

        tvLoginLabel = view.findViewById(R.id.tvLoginLabel);

        etUserName = view.findViewById(R.id.etUserName);
        etMainPassword = view.findViewById(R.id.etMainPassword);
        etSecondaryPassword = view.findViewById(R.id.etSecondaryPassword);
        etPhone = view.findViewById(R.id.etPhone);
        etMainEmail = view.findViewById(R.id.etMainEmail);
        etSecondaryEmail = view.findViewById(R.id.etSecondaryEmail);

        etUserNameLayout= view.findViewById(R.id.etUserNameLayout);
        etMainPasswordLayout= view.findViewById(R.id.etMainPasswordLayout);
        etSecondaryPasswordLayout= view.findViewById(R.id.etSecondaryPasswordLayout);
        etPhoneLayout= view.findViewById(R.id.etPhoneLayout);
        etMainEmailLayout= view.findViewById(R.id.etMainEmailLayout);
        etSecondaryEmailLayout= view.findViewById(R.id.etSecondaryEmailLayout);

        btnToRegister = view.findViewById(R.id.btnToRegister);
        btnLoginCancel = view.findViewById(R.id.btnLoginCancel);
        btnLoginSubmit = view.findViewById(R.id.btnLoginSubmit);
        btnLoginButton = view.findViewById(R.id.btnLoginButton);
        btnLoginButton.setOnClickListener(loginClick);
        btnLoginSubmit.setOnClickListener(submitClick);
        btnToRegister.setOnClickListener(registerClick);
        btnLoginCancel.setOnClickListener(cancelClick);

        showLogin();
    }
    public void showLogin(){
        isRegister = false;
        tvLoginLabel.setText(R.string.login_label);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) llLoginData.getLayoutParams();
        params.verticalBias = 0.65f;
        llLoginData.setLayoutParams(params);

        btnToRegister.setVisibility(View.VISIBLE);
        btnLoginButton.setVisibility(View.VISIBLE);
        btnLoginSubmit.setVisibility(View.GONE);
        btnLoginCancel.setVisibility(View.GONE);

        etPhoneLayout.setVisibility(View.GONE);
        etMainEmailLayout.setVisibility(View.GONE);
        etSecondaryEmailLayout.setVisibility(View.GONE);
        etSecondaryPasswordLayout.setVisibility(View.GONE);
    }
    public void showRegister(){
        isRegister = true;
        tvLoginLabel.setText(R.string.register_label);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) llLoginData.getLayoutParams();
        params.verticalBias = 1.0f;
        llLoginData.setLayoutParams(params);

        btnToRegister.setVisibility(View.GONE);
        btnLoginButton.setVisibility(View.GONE);
        btnLoginSubmit.setVisibility(View.VISIBLE);
        btnLoginCancel.setVisibility(View.VISIBLE);

        etPhoneLayout.setVisibility(View.VISIBLE);
        etMainEmailLayout.setVisibility(View.VISIBLE);
        etSecondaryEmailLayout.setVisibility(View.VISIBLE);
        etSecondaryPasswordLayout.setVisibility(View.VISIBLE);

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
        etPhoneLayout.setError(null);
        etUserNameLayout.setError(null);
        etMainEmailLayout.setError(null);
        etMainPasswordLayout.setError(null);
        etSecondaryEmailLayout.setError(null);
        etSecondaryPasswordLayout.setError(null);
    }
    public void showError(LoginRegisterError error) {
        switch (error){
            case EmailEmptyError:
                etMainEmailLayout.setError(res.getString(R.string.register_email_empty_error));
                break;
            case EmailNotMatchError:
                etMainEmailLayout.setError(res.getString(R.string.register_email_not_match_error));
                etSecondaryEmailLayout.setError(res.getString(R.string.register_email_not_match_error));
                break;
            case PasswordWrongError:
                etMainPasswordLayout.setError(res.getString(R.string.register_password_wrong_error));
                break;
            case PasswordEmptyError:
                etMainPasswordLayout.setError(res.getString(R.string.register_password_empty_error));
                break;
            case PasswordNotMatchError:
                etMainPasswordLayout.setError(res.getString(R.string.register_password_not_match_error));
                etSecondaryPasswordLayout.setError(res.getString(R.string.register_password_not_match_error));
                break;
            case PhoneLengthError:
                etPhoneLayout.setError(res.getString(R.string.register_phone_length_error));
                break;
            case UserNameWrongError:
                etUserNameLayout.setError(res.getString(R.string.register_username_wrong_error));
                break;
            case UserNameEmptyError:
                etUserNameLayout.setError(res.getString(R.string.register_username_empty_error));
                break;
            case UserNameTakenError:
                etUserNameLayout.setError(res.getString(R.string.register_username_taken_error));
                break;
        }
    }
}
