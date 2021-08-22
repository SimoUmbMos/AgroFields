package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Address;
import android.os.AsyncTask;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

public class LandInfoHolder {
    private final LandInfoHolderActions actions;
    private final Resources resources;
    private final TextView tvLandNameLabel;
    private final EditText etLandName,etAddress;
    private final Button btnCancel,btnSubmit;

    public LandInfoHolder(View view, Resources resources,LandInfoHolderActions actions){
        this.resources = resources;
        this.actions = actions;
        tvLandNameLabel = view.findViewById(R.id.tvLandNameLabel);
        etLandName = view.findViewById(R.id.etLandName);
        etAddress = view.findViewById(R.id.etAddress);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnSubmit);
    }

    public void init(Activity activity, Land land) {
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
            etAddress.setEnabled(true);
        }else{
            landLabel = resources.getString(R.string.edit_land_label);
            etLandName.setText(land.getData().getTitle());
            etAddress.setEnabled(false);
            asyncFindAddress(activity,land);
        }
        tvLandNameLabel.setText(landLabel);
    }

    private void asyncFindAddress(Activity activity, Land land) {
        if(activity != null){
            AsyncTask.execute(()->{
                Address address = MapUtil.findLocation(
                        activity,
                        MapUtil.getPolygonCenter(LandUtil.getLatLngPoints(land))
                );
                if(address!= null){
                    String tempDisplay = null;
                    if(address.getLocality() != null){
                        if(address.getCountryName() != null){
                            tempDisplay = address.getLocality()+", "+address.getCountryName();
                        }else{
                            tempDisplay = address.getLocality();
                        }
                    }
                    final String finalDisplay = tempDisplay;
                    if(finalDisplay != null){
                        activity.runOnUiThread(()->etAddress.setText(finalDisplay));
                    }
                }
            });
        }
    }

    private void onSubmit(View view) {
        actions.onSubmit(
                etLandName.getText().toString(),
                etAddress.getText().toString()
        );
    }
    private void onCancel(View view) {
        actions.onCancel();
    }

    public interface LandInfoHolderActions{
        void onSubmit(String landName,String address);
        void onCancel();
    }
}
