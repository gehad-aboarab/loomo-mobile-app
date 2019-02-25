package com.senior491.mobileapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

public class App extends Application {

    //Error strings
    public static final String BLUETOOTH_UNSUPPORTED = "Bluetooth not supported.";
    public static final String BLE_UNSUPPORTED = "BLE not supported.";
    public static final String LOCATION_REQUIRED = "Location permissions required.";
    public static final String CANNOT_LOCATE = "Cannot locate you, please try again later.";
    public static final String LOOMO_UNAVAILABLE = "Loomo not available currently, please try again later.";
    public static final String SERVER_ERROR = "Server error occurred, please try again later.";

    //User message strings
    public static final String RETRIEVE_LOCATION = "Retrieving your location.\nPlease wait..";
    public static final String LOOMO_AVAILABLE = "Loomo on its way!";
    public static final String DISMISSAL_SUCCESSFUL = "Loomo dismissed!";

    //Route strings
    public static final String TOPIC_SERVER_TO_MOBILE = "server-to-mobile";
    public static final String TOPIC_MOBILE_TO_SERVER = "mobile-to-server";
    public static final String S2M_LOOMO_STATUS = "server-to-mobile/loomo-status";
    public static final String S2M_LOOMO_ARRIVAL = "server-to-mobile/loomo-arrival";
    public static final String S2M_USER_DESTINATION = "server-to-mobile/user-destination";
    public static final String S2M_ERROR = "server-to-mobile/error";
    public static final String S2M_LOOMO_DISMISSAL = "server-to-mobile/loomo-dismissal";
    public static final String M2S_BEACON_SIGNALS = "mobile-to-server/beacon-signals";
    public static final String M2S_USER_DESTINATION = "mobile-to-server/user-destination";

    public String deviceId;
    public MqttHelper mqttHelper;
    private final String TAG = "SeniorSucks_App";

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d(TAG, deviceId);
        mqttHelper = new MqttHelper(getApplicationContext());
    }

}