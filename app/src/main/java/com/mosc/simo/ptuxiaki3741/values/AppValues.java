package com.mosc.simo.ptuxiaki3741.values;

import android.graphics.Color;

import com.mosc.simo.ptuxiaki3741.models.ColorData;

public final class AppValues {
    private AppValues(){}
    /* DB VALUES */
    public static final int DATABASE_VERSION = 37;
    /* Open XML SDK */
    public static final String sheetLandName = "Lands";
    public static final String sheetLandNameLowerCase = "lands";
    public static final String sheetLandZoneName = "Zones";
    public static final String sheetLandZoneNameLowerCase = "zones";
    /* Common Values */
    public static final int defaultPadding = 64;
    public static final int defaultPaddingLite = 16;
    public static final int defaultStrokeAlpha = 160;
    public static final int defaultFillAlpha = 80;
    public static final ColorData defaultLandColor = new ColorData(20, 249, 80);
    public static final ColorData defaultZoneColor = new ColorData(80, 20, 249);
    public static final int defaultPersonColorFill = Color.parseColor("#3777f1");
    public static final int defaultPersonColorStroke = Color.parseColor("#ffffff");
    /* Args Values */
    public static final String argLand = "land";
    public static final String argLands = "lands";
    public static final String argZone = "zone";
    public static final String argAction = "action";
    public static final String argAddress = "address";
    public static final String argImportLand = "import_land";
    public static final String argIsHistory = "is_history";
    public static final String argNotification = "calendar_notification";
    /* Calendar Fragment Values */
    public static int defaultDayColor1 = Color.argb(192,128,128,128);
    public static int defaultDayColor2 = Color.argb(64,128,128,128);
    /* Land Map Editor Fragment Values */
    public static final double distanceToMapActionKM = 100;
    public static final float countryZoom = 6.0f;
    public static final float cityZoom = 13.0f;
    public static final float streetZoom = 16.0f;
    /* Live Map Fragment Values */
    public static final int AnimationRotate = 140;
    public static final float defaultTilt = 67.5f;
    public static final float locationSensitivity = 1.0f;
    public static final float bearingSensitivity = 1.0f;
    public static final double currPositionSize = 4.0;
    public static final float currPositionSizeStroke = 6.0f;
    public static final float personZoom = 18.0f;
    /* Main Activity Values */
    public static final String isForceKey = "is_force_theme";
    public static final String isDarkKey = "is_dark_theme";
    public static final int doubleTapBack = 2750;
    public static final String NotificationChannelID = "";
}
