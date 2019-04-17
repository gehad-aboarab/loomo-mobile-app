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
    private TextView textView;
    private boolean observing;
    private App application;

    private String mode;
    private static final int REQUEST_PERMISSIONS_CALLBACK = 0;

    private String nearestBeaconTag;

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
        Log.d("Senior", "Starting to observe...");
        EstimoteCloudCredentials estimoteCloudCredentials =
                new EstimoteCloudCredentials("loomo-app-test-cef",
                        "dd5b75f96aae79e9b94496671f6e9dbc");

        proximityObserver = new ProximityObserverBuilder(application, estimoteCloudCredentials).build();
        observing = true;

        String[] tags = {"blueberry_b", "coconut_b", "icy_b", "mint_b", "blueberry_a", "coconut_a", "icy_a", "mint_a"};
        ProximityZone[] zones = new ProximityZone[tags.length];
        for (int i = 0; i < zones.length; ++i)
        {
            Log.d("Senior", "Added zone " + tags[i]);
            zones[i] = new ProximityZoneBuilder().forTag(tags[i]).inCustomRange(2)
                    .onEnter(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext proximityZoneContext) {
                            Log.d("Senior", "Entered zone: " + proximityZoneContext.getTag());
                            nearestBeaconTag = proximityZoneContext.getTag();
                            return null;
                        }
                    })
                    .onExit(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext proximityZoneContext) {
                            nearestBeaconTag = null;
                            return null;
                        }
                    }).build();
        }

        proximityObserver.startObserving(zones);
    }

    public void stopObserving() {
        observing = false;
        proximityObserver.startObserving(new ProximityZone[]{});
    }

    public String getNearestBeaconTag() {
        return nearestBeaconTag;
    }

    public String getMode() {
        return mode;
    }
}
