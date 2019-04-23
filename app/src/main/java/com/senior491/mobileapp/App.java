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
    public static final String S2M_GET_TOURS = "server-to-mobile/get-tours";
    public static final String S2M_LOOMO_DISMISS = "server-to-mobile/loomo-dismiss";
    public static final String S2M_JOURNEY_STARTED = "server-to-mobile/started-journey";
    public static final String S2M_JOURNEY_ENDED = "server-to-mobile/end-journey";
    public static final String S2M_ERROR = "server-to-mobile/error";
    public static final String M2S_LOOMO_DISMISSAL = "mobile-to-server/loomo-dismiss";
    public static final String M2S_BEACON_SIGNALS = "mobile-to-server/beacon-signals";
    public static final String M2S_GET_MAP_DESTINATIONS = "mobile-to-server/get-map-destinations";
    public static final String M2S_GET_TOURS = "mobile-to-server/get-tours";
    public static final String M2S_START_JOURNEY = "mobile-to-server/start-journey";
    public static final String DEFAULT_DESTINATION = "Please select a destination";
    public static final String DEFAULT_TOUR = "Please select a tour";
    public static final String TAG = "SeniorSucks_App";

    public static final int TOUR_MODE = 2;
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
    public int currentMode;
    public String currentDestination;
    public String currentTour;
    public String currentBeacon = "5812ca89ff64bf356564f5ee641f6f1b";

    public MqttHelper mqttHelper;
    public String mapName = "EB2-Rotunda";
    public String tourName = "EB2-Rotunda";
    public ArrayList<String> destinations;
    public ArrayList<String> tours;
    public ArrayList<String> beacons;

    @Override
    public void onCreate() {
        super.onCreate();

        clientId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "My device ID: " + clientId);
        destinations = new ArrayList<>();
        tours = new ArrayList<>();
        beacons = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        loomoId = sp.getString("loomoId",null);
        if (loomoId != null)
            currentState = sp.getInt("currentState", UNBOUND);
        else
            currentState = UNBOUND;

        mqttHelper = new MqttHelper(this);
    }

    public void updateCurrentMode(int mode){
        currentMode = mode;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putInt("mode", currentMode);
        editor.commit();
    }

    public void updateCurrentDestination(String destination){
        currentDestination = destination;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putString("destination", currentDestination);
        editor.commit();
    }

    public void updateCurrentTour(String tour){
        currentTour = tour;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putString("tour", currentTour);
        editor.commit();
    }

    public void updateCurrentState(int state){
        currentState = state;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putInt("state", currentState);
        editor.commit();
    }

    public void updateLoomoId(String loomoId){
        this.loomoId = loomoId;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putString("loomoId", this.loomoId);
        editor.commit();
    }

    public void updateBeacon(String beacon){
        this.currentBeacon = beacon;
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putString("beacon", this.currentBeacon);
        editor.commit();
    }

}