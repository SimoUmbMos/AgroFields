package com.mosc.simo.ptuxiaki3741.values;

import android.graphics.Color;

public final class AppValues {
    /* DB VALUES*/
    public static final int DATABASE_VERSION = 27;
    /* ViewModels VALUES */
    public static final String sharedPreferenceKeyUserViewModel = "currUser";
    public static final long sharedPreferenceDefaultUserViewModel = -1;
    /* Common Values*/
    public static final int defaultPadding = 16;
    public static final int strokeColor = Color.argb(192,0,0,255);
    public static final int fillColor = Color.argb(51,0,0,255);
    /* Contact Profile Fragment Values */
    public static final String CONTACT_PROFILE_ARG = "user";
    /* Share Land Fragment Values */
    public static final String SHARE_LAND_USER_ARG = "user";
    public static final String SHARE_LAND_DATA_ARG = "land";
    /* Land Info Fragment Values */
    public static final String argLandInfoFragment = "land";
    /* Land Map Preview Fragment Values*/
    public static final String argLandLandMapPreviewFragment = "land";
    public static final String argIsHistoryLandMapPreviewFragment= "is_history";
    /* Land Map Editor Fragment Values*/
    public static final String argLandLandMapFragment = "land";
    public static final String argImportLandData = "import_land";
    public static final String argAddressLandMapFragment = "address";
    public static final double distanceToMapActionKM = 100;
    public static final float stepOpacity = 0.03f;
    public static final float stepRotate = 1f;
    public static final float stepZoom = 0.03f;
    public static final float maxZoom = 7f;
    public static final float minZoom = 0.1f;
    public static final float countryZoom = 5.0f;
    public static final float cityZoom = 10.0f;
    public static final float streetZoom = 15.0f;
    /* Main Activity Values */
    public static final String isForceKey = "is_force_theme";
    public static final String isDarkKey = "is_dark_theme";
    public static final int doubleTapBack = 2750;
    public static final int backgroundInterval = 600000;
    /* Import Activity Values */
    public static final String argImportFragLandDataList = "land_data_list";
    public static final String argImportFragCurrLandData = "land_data";
    public static final String argImportFragLandAction = "file_action";
}
