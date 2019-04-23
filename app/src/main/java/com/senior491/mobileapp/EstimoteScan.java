package com.senior491.mobileapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityObserverConfiguration;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.util.ArrayList;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EstimoteScan extends Service {
    private ProximityObserver proximityObserver;
    private ProximityObserver.Handler handler;

    private App application;
    private boolean observing;
    private boolean stable;
    private String mode;
    private String nearestBeaconTag;
    private String nearestBeaconId;

    private static final int REQUEST_PERMISSIONS_CALLBACK = 0;
    private static final String TAG = "SeniorSucksEstimoteScan";

//    public EstimoteScan(App application){
//        this.application = (App) application;
//        if(application.currentMode == application.GUIDE_MODE) {
//            this.mode = "guide";
//        } else if(application.currentMode == application.RIDE_MODE){
//            this.mode = "ride";
//        } else if(application.currentMode == application.TOUR_MODE){
//            this.mode = "tour";
//        }
//
//        Log.d(TAG, "Scan constructor called");
//    }

    public boolean isObserving(){
        return observing;
    }

    public void startObserving() {
        if (observing) {
            Log.d("Senior", "WARNING: startObserving called twice, ignoring the second call!");
            return;
        }

        Log.d(TAG, "Starting to observe...");
        EstimoteCloudCredentials estimoteCloudCredentials =
                new EstimoteCloudCredentials("loomo-app-test-cef",
                        "dd5b75f96aae79e9b94496671f6e9dbc");

        proximityObserver = new ProximityObserverBuilder(application, estimoteCloudCredentials).build();
        observing = true;
//        stable = false;

        Log.d(TAG, application.beacons.size() + "");
        ProximityZone[] zones = new ProximityZone[application.beacons.size()];
        ProximityZoneBuilder builder = new ProximityZoneBuilder();
        for (int i = 0; i < zones.length; ++i)
        {
            final String beacon = application.beacons.get(i);
            Log.d(TAG, "Added zone " + beacon);

            zones[i] = builder.forTag(beacon).inCustomRange(1.5)
                    .onEnter(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext proximityZoneContext) {
                            stable = true;
                            Log.d(TAG, "Entered zone: " + proximityZoneContext.getDeviceId());
                            nearestBeaconTag = proximityZoneContext.getTag();
                            nearestBeaconId = proximityZoneContext.getDeviceId();
                            application.updateBeacon(nearestBeaconTag);
                            return null;
                        }
                    }).build();
        }

        handler = proximityObserver.startObserving(zones);
    }

    public void stopObserving() {
        observing = false;
        handler.stop();
    }

    public String getNearestBeaconTag() {
        return nearestBeaconTag;
    }

    public String getNearestBeaconId() { return nearestBeaconId; }

    public boolean isStable() {
        return stable;
    }

    public String getMode() {
        return mode;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        application = (App) getApplication();
        startObserving();
        return super.onStartCommand(intent, flags, startId);
    }
}
