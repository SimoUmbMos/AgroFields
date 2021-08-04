package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.View;
import android.widget.Button;

import com.mosc.simo.ptuxiaki3741.R;

public class MainMenuHolder {
    public Button btnList, btnLogout, btnHistory, btnFriends, btnProfile;

    public MainMenuHolder(View view){
        btnList = view.findViewById(R.id.btnList);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnFriends = view.findViewById(R.id.btnFriends);
        btnProfile = view.findViewById(R.id.btnProfile);
    }
}
