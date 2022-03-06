package com.mosc.simo.ptuxiaki3741.data.values;

import com.mosc.simo.ptuxiaki3741.data.models.ColorData;

public final class AppValues {

    private AppValues(){}
    /* DB VALUES */
    public static final int DATABASE_VERSION = 1;
    /* Open XML SDK */
    public static final String sheetLandName = "Lands";
    public static final String sheetLandNameLowerCase = "lands";
    public static final String sheetLandZoneName = "Zones";
    public static final String sheetLandZoneNameLowerCase = "zones";
    /* Common Values */
    public static final int defaultPadding = 64;
    public static final int defaultPaddingLite = 16;
    public static final int defaultPaddingLarge = 128;
    public static final int defaultStrokeAlpha = 160;
    public static final int defaultFillAlpha = 80;
    public static final ColorData defaultLandColor = new ColorData("#FF9100");
    public static final ColorData defaultZoneColor = new ColorData("#76FF03");
    public static final float minZoom = 1.0f;
    public static final float maxZoom = 19.0f;
    /* Args Values */
    public static final String argSnapshotKey = "data_snapshot_key";
    public static final String argLand = "land";
    public static final String argLands = "lands";
    public static final String argZone = "zone";
    public static final String argZones = "zones";
    public static final String argAction = "action";
    public static final String argAddress = "address";
    public static final String argImportLand = "import_land";
    public static final String argIsHistory = "is_history";
    public static final String argDate = "calendar_date";
    public static final String argNotification = "calendar_notification";
    public static final String argListView = "calendar_list_view";
    /* Land Map Editor Fragment Values */
    public static final double distanceToMapActionKM = 100;
    public static final float countryZoom = 6.0f;
    public static final float cityZoom = 13.0f;
    public static final float streetZoom = 16.0f;
    /* Live Map Fragment Values */
    public static final float locationSensitivity = 1.0f;
    public static final float bearingAlphaHigh = 0.15f;
    public static final float bearingAlphaMedium = 0.075f;
    public static final float bearingAlphaLow = 0.0375f;
    public static final float bearingAlphaNone = 0.01875f;
    /* Settings Fragment Values */
    public static final String ownerName = "AppOwnerName";
    public static final String ownerEmail = "AppOwnerEmail";
    /* Main Activity Values */
    public static final String isForceKey = "is_force_theme";
    public static final String isDarkKey = "is_dark_theme";
    public static final int doubleTapBack = 2750;
    public static final String NotificationChannelID = "In Application Notifications";
    public static final String CalendarNotificationChannelID = "Calendar Notifications";
}
