package com.senior491.mobileapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadingActivity extends Activity {
    private LeDeviceList mLeDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private App application;

    private String status;
    public static ProgressBar progressBar;
    public static TextView statusTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        getActionBar().setTitle(R.string.title_devices);

        application = (App) getApplication();

        status = getIntent().getStringExtra("status");
        progressBar = (ProgressBar) findViewById(R.id.progress);
        statusTextView = (TextView) findViewById(R.id.status);
//        progressBar.setVisibility(View.VISIBLE);

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    statusTextView.setText(status);
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    Toast.makeText(this, "Try again!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        mLeDeviceList = new LeDeviceList();
        scanLeDevice(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceList.clear();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothScanner.stopScan(mLeScanCallback);

                    Log.d("Mqtt", "hello1");
                    validLocation();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothScanner.startScan(mLeScanCallback);

        } else {
            mScanning = false;
            Log.d("Mqtt", "hello2");
            validLocation();
            mBluetoothScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    public void validLocation(){
        if (mLeDeviceList.getCount() >= 3) {
            MqttMessage msg = new MqttMessage();
            JSONObject obj = new JSONObject();
            try {
                JSONObject signals = new JSONObject();
                JSONArray signalsArray = new JSONArray();

                for(int i=0; i<mLeDeviceList.getCount(); i++){
                    signals.put(Integer.toString(i), mLeDeviceList.mLeRssis.get(i));
                }
                signalsArray.put(signals);

                obj.put("clientID", application.deviceId);
                obj.put("BeaconSignals", signalsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg.setPayload(obj.toString().getBytes());
            try {
                application.mqttHelper.mqttAndroidClient.publish("mobile-to-server",msg);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            statusTextView.setText("Loomo on its way...");

            //simulate loomo on its way to user's location
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MainActivity.setLoomoPresent(true);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {
            statusTextView.setText("Cannot locate you, please try again later.");
            progressBar.setVisibility(View.GONE);
        }
    }

    private class LeDeviceList {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<Integer> mLeRssis;
        private ArrayList<String> mLeIds;

        public LeDeviceList() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mLeRssis = new ArrayList<Integer>();
            mLeIds = new ArrayList<String>();
        }

        public void addDevice(BluetoothDevice device, String id, int rssi) {
            if (!mLeIds.contains(id)) {
                mLeDevices.add(device);
                mLeIds.add(id);
                mLeRssis.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public int getRssi(int position) {
            return mLeRssis.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            mLeRssis.clear();
            mLeIds.clear();
        }

        public int getCount() {
            return mLeIds.size();
        }
    }

    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                            ArrayList<String> beacons = new ArrayList<String>();

                            beacons.add("59bfdda585767280f886db284653ee35"); //Icy B
                            beacons.add("283acdcf5be28c0f71dc4b6a84219d29"); //Icy A
                            beacons.add("5812ca89ff64bf356564f5ee641f6f1b"); //Mint B
                            beacons.add("6a811095d963f29290ea5371b4177020"); //Mint A
                            beacons.add("3c52a5930c34db229451868164d7fc13"); //Coconut B
                            beacons.add("4454649ebee76a8e5f23a202825c8401"); //Coconut A
                            beacons.add("e158516ea666f214c38d5464c5440d1f"); //Blueberry B
                            beacons.add("d9b0b6f879088d8f767576e07841e43a"); //Blueberry A

                            byte[] bytes = result.getScanRecord().getServiceData(ParcelUuid.fromString("0000fe9a-0000-1000-8000-00805f9b34fb"));
                            if(bytes != null) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 1; i < bytes.length; i++) {
                                    if (i <= 16)
                                        sb.append(String.format("%02x", bytes[i]));
                                }
                                if(beacons.contains(sb.toString())) {
                                    BluetoothDevice device = result.getDevice();
                                    String id = sb.toString();
                                    int rssi = result.getRssi();
                                    Log.d("GEHAD", "identifier: " + id + " address: " + device.getAddress());
                                    mLeDeviceList.addDevice(device, id, rssi);
                                }
                            }
                        }
                    });
                }
            };

}
