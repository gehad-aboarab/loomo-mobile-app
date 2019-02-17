package com.senior491.mobileapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;

public class App extends Application {
    public String deviceId;
    public MqttHelper mqttHelper;

    public App() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        deviceId = wInfo.getMacAddress();
        Log.d("Mqtt", deviceId);
//        deviceId="09";
//        deviceId = UUID.randomUUID().toString();
        mqttHelper = new MqttHelper(getApplicationContext());
    }
}
