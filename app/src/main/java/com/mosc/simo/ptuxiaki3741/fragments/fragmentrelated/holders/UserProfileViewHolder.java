package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.mosc.simo.ptuxiaki3741.R;

public class UserProfileViewHolder {
    public TextView tvTitle;
    public Button btnModify;
    public TextInputEditText etPhone, etEmail;
    private boolean isEditMode;
    public UserProfileViewHolder(View v, View.OnClickListener onModify){
        tvTitle = v.findViewById(R.id.tvTitle);
        etPhone = v.findViewById(R.id.etPhone);
        etEmail = v.findViewById(R.id.etEmail);
        btnModify = v.findViewById(R.id.btnModify);
        btnModify.setOnClickListener(onModify);
        setEditMode(false);
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        etEmail.setEnabled(isEditMode);
        etPhone.setEnabled(isEditMode);
    }

    public boolean isEditMode(){
        return isEditMode;
    }
}
