package com.senior491.mobileapp;

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
    public static final String ONGOING_JOURNEY = "Your journey is ongoing..";
    public static final String LOOMO_AVAILABLE = "Loomo on its way!";
    public static final String DISMISSAL_SUCCESSFUL = "Loomo dismissed!";

    //Route strings
    public static final String TOPIC_SERVER_TO_MOBILE = "server-to-mobile";
    public static final String TOPIC_MOBILE_TO_SERVER = "mobile-to-server";
    public static final String S2M_LOOMO_STATUS = "server-to-mobile/loomo-status";
    public static final String S2M_LOOMO_ARRIVAL = "server-to-mobile/loomo-arrival";
    public static final String S2M_GET_MAP_DESTINATIONS = "server-to-mobile/get-map-destinations";
    public static final String S2M_LOOMO_DISMISS = "server-to-mobile/loomo-dismiss";
    public static final String S2M_JOURNEY_STARTED = "server-to-mobile/started-journey";
    public static final String S2M_JOURNEY_ENDED = "server-to-mobile/end-journey";
    public static final String S2M_ERROR = "server-to-mobile/error";
    public static final String M2S_LOOMO_DISMISSAL = "mobile-to-server/loomo-dismiss";
    public static final String M2S_BEACON_SIGNALS = "mobile-to-server/beacon-signals";
    public static final String M2S_GET_MAP_DESTINATIONS = "mobile-to-server/get-map-destinations";
    public static final String M2S_START_JOURNEY = "mobile-to-server/start-journey";

    public static final String DEFAULT_DESTINATION = "Please select a destination";

    public static final int GUIDE_MODE = 1;
    public static final int RIDE_MODE = 0;

    public String deviceId;
    public String loomoId;
    public MqttHelper mqttHelper;
    public boolean usingLoomo = false;
    public String mapName = "EB1-Rotunda";
    public ArrayList<String> destinations;
    private final String TAG = "SeniorSucks_App";

    @Override
    public void onCreate() {
        super.onCreate();

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, deviceId);
        destinations = new ArrayList<>();
        mqttHelper = new MqttHelper(this);
    }

}