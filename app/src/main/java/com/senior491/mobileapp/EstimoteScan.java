package com.senior491.mobileapp;

import android.util.Log;
import android.widget.TextView;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EstimoteScan {
    private ProximityObserver proximityObserver;
    private ProximityObserver.Handler handler;

    private App application;

    private boolean observing;
    private String mode;
    private String nearestBeaconTag;
    private String nearestBeaconId;

    private static final int REQUEST_PERMISSIONS_CALLBACK = 0;

    public EstimoteScan(App application){
        this.application = (App) application;
        if(application.currentMode == application.GUIDE_MODE) {
            this.mode = "guide";
        } else if(application.currentMode == application.RIDE_MODE){
            this.mode = "ride";
        } else if(application.currentMode == application.TOUR_MODE){
            this.mode = "tour";
        }
    }

    public boolean isObserving(){
        return observing;
    }

    public void startObserving() {
        if (observing) {
            Log.d("Senior", "WARNING: startObserving called twice, ignoring the second call!");
            return;
        }

        Log.d("Senior", "Starting to observe...");
        EstimoteCloudCredentials estimoteCloudCredentials =
                new EstimoteCloudCredentials("loomo-app-test-cef",
                        "dd5b75f96aae79e9b94496671f6e9dbc");

        proximityObserver = new ProximityObserverBuilder(application, estimoteCloudCredentials).build();
        observing = true;

        ProximityZone[] zones = new ProximityZone[application.beacons.size()];
        for (int i = 0; i < zones.length; ++i)
        {
            String beacon = application.beacons.get(i);
            Log.d("Senior", "Added zone " + beacon);
            zones[i] = new ProximityZoneBuilder().forTag(beacon).inCustomRange(2)
                    .onEnter(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext proximityZoneContext) {
                            Log.d("Senior", "Entered zone: " + proximityZoneContext.getTag());
                            nearestBeaconTag = proximityZoneContext.getTag();
                            nearestBeaconId = proximityZoneContext.getDeviceId();
                            return null;
                        }
                    })
                    .onExit(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext proximityZoneContext) {
                            nearestBeaconTag = null;
                            nearestBeaconId = proximityZoneContext.getDeviceId();
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

    public String getMode() {
        return mode;
    }
}
