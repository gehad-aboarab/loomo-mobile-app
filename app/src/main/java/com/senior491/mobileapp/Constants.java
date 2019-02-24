package com.senior491.mobileapp;

/**
 * Created by Gehad on 2/24/2019.
 */

public class Constants {
    //Error strings
    private static final String BLUETOOTH_UNSUPPORTED = "Bluetooth not supported.";
    private static final String BLE_UNSUPPORTED = "BLE not supported.";
    private static final String LOCATION_REQUIRED = "Location permissions required.";
    private static final String CANNOT_LOCATE = "Cannot locate you, please try again later.";
    private static final String LOOMO_UNAVAILABLE = "Loomo not available currently, please try again later.";
    private static final String SERVER_ERROR = "Server error occurred, please try again later.";

    //User message strings
    public static final String RETRIEVE_LOCATION = "Retrieving your location.\nPlease wait..";
    public static final String LOOMO_AVAILABLE = "Loomo on its way!";
    public static final String DISMISSAL_SUCCESSFUL = "Loomo dismissed!";

    //Route strings
    public static final String TOPIC_SERVER_TO_MOBILE = "server-to-mobile";
    public static final String TOPIC_MOBILE_TO_SERVER = "mobile-to-server";
    public static final String ROUTE_LOOMO_STATUS = "server-to-mobile/loomo-status";
    public static final String ROUTE_LOOMO_ARRIVAL = "server-to-mobile/loomo-arrival";
    public static final String ROUTE_USER_DESTINATION = "server-to-mobile/user-destination";
    public static final String ROUTE_ERROR = "server-to-mobile/error";
    public static final String ROUTE_LOOMO_DISMISSAL = "server-to-mobile/loomo-dismissal";


}
