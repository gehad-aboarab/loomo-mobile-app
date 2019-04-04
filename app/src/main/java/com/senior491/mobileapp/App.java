package com.senior491.mobileapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

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
    public static final String SHARED_PREF_FILE = "sharedprefs";
    public static final String RETRIEVE_LOCATION = "Retrieving your location.\nPlease wait..";
    public static final String ONGOING_JOURNEY = "Your journey is ongoing..";
    public static final String LOOMO_AVAILABLE = "Loomo on its way!";
    public static final String DISMISSAL_SUCCESSFUL = "Loomo dismissed!";

    //Route strings
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
    public static final String TAG = "SeniorSucks_App";

    public static final int GUIDE_MODE = 1;
    public static final int RIDE_MODE = 0;

    public static final int UNBOUND = 100;
    public static final int BOUND_WAITING = 101;
    public static final int BOUND_JOURNEY_STARTABLE = 102;
    public static final int BOUND_ONGOING_JOURNEY = 103;
    public static final int BOUND_JOURNEY_ENDED = 104;

    public String clientId;
    public String loomoId;
    public int currentState;
    public MqttHelper mqttHelper;
    public String mapName = "EB1-Rotunda";
    public ArrayList<String> destinations;
//    public boolean usingLoomo = false;

    @Override
    public void onCreate() {
        super.onCreate();

        clientId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "My device ID: " + clientId);
        destinations = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        loomoId = sp.getString("loomoId",null);
        if (loomoId != null)
            currentState = sp.getInt("currentState", UNBOUND);
        else
            currentState = UNBOUND;


        mqttHelper = new MqttHelper(this);

    }

}