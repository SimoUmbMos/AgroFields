package com.mosc.simo.ptuxiaki3741.fragmentrelated.holders;

import android.content.res.Resources;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;

public class LandInfoHolder {
    private final LandInfoHolderActions actions;
    private final Resources resources;
    private final TextView tvLandNameLabel;
    private final EditText etLandName;
    private final Button btnCancel,btnSubmit;

    public LandInfoHolder(View view, Resources resources,LandInfoHolderActions actions){
        this.resources = resources;
        this.actions = actions;
        tvLandNameLabel = view.findViewById(R.id.tvLandNameLabel);
        etLandName = view.findViewById(R.id.etLandName);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnSubmit);
    }

    public void init(Land land) {
        etLandName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSubmit(v);
                return true;
            }
            return false;
        });
        btnSubmit.setOnClickListener(this::onSubmit);
        btnCancel.setOnClickListener(this::onCancel);

        String landLabel;
        if(land == null){
            landLabel = resources.getString(R.string.create_land_label);
        }else{
            landLabel = resources.getString(R.string.edit_land_label);
            etLandName.setText(land.getData().getTitle());
        }
        tvLandNameLabel.setText(landLabel);
    }

    private void onSubmit(View view) {
        actions.onSubmit(etLandName.getText().toString());
    }
    private void onCancel(View view) {
        actions.onCancel();
    }

    public interface LandInfoHolderActions{
        void onSubmit(String landName);
        void onCancel();
    }
}
