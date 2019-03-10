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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class LoadingActivity extends Activity {

    private int status;
    private ProgressBar progressBar;
    private TextView statusTextView;

    private Handler handler;
    private BluetoothManager bluetoothManager;
    private LeDeviceList leDeviceList;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private boolean mScanning;
    private App application;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private static final long SCAN_PERIOD = 4000;
    private static final String TAG = "SeniorSucks_Scan";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        application = (App) getApplication();
//        getActionBar().setTitle(R.string.title_devices);

        //GUI initializations
        progressBar = (ProgressBar) findViewById(R.id.progress);
        statusTextView = (TextView) findViewById(R.id.status);
        status = getIntent().getIntExtra("status", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On resume called.");

        //Bluetooth permissions and initializations
        handler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, application.BLE_UNSUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, application.BLUETOOTH_UNSUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        //Location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            if (status == 0) {
                statusTextView.setText(application.RETRIEVE_LOCATION);
                progressBar.setVisibility(View.VISIBLE);
            }
            if (!bluetoothAdapter.isEnabled()) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
            leDeviceList = new LeDeviceList();
            scanLeDevice(true);

            startMqtt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (status == 0) {
                        statusTextView.setText(application.RETRIEVE_LOCATION);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    return;
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    Toast.makeText(this, application.LOCATION_REQUIRED, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }
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
        Log.d(TAG, "On pause called.");
        super.onPause();
        scanLeDevice(false);
        leDeviceList.clear();
    }

    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "Scan method called.");
        if (enable) {
            //Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    validLocation();
                    bluetoothScanner.stopScan(leScanCallBack);
                    Log.d(TAG, "Scanning stopped.");
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothScanner.startScan(leScanCallBack);
        }
    }

    public void validLocation(){
        if (leDeviceList.getCount() >= 3) {
            //Connection to server
            MqttMessage msg = new MqttMessage();
            JSONObject obj = new JSONObject();
            try {
                JSONObject signals = new JSONObject();
                JSONArray signalsArray = new JSONArray();

                for(int i=0; i<leDeviceList.getCount(); i++) {
                    signals.put(leDeviceList.mLeIds.get(i), leDeviceList.mLeDevices.get(leDeviceList.mLeIds.get(i)));
                    signalsArray.put(signals);
                }

                obj.put("clientID", application.deviceId);
                obj.put("BeaconSignals", signalsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg.setPayload(obj.toString().getBytes());
            Log.d(TAG, msg.toString());
            try {
                Log.d(TAG, "publishing msg");
                application.mqttHelper.mqttAndroidClient.publish(application.M2S_BEACON_SIGNALS, msg);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(this, application.CANNOT_LOCATE, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class LeDeviceList {
        private ArrayList<BluetoothDevice> mLeDevice;
        private ArrayList<Integer> mLeRssis;
        private ArrayList<String> mLeIds;
        private HashMap<String, ArrayList<Integer>> mLeDevices;

        public LeDeviceList() {
            super();
            mLeDevices = new HashMap<>();
            mLeRssis = new ArrayList<Integer>();
            mLeIds = new ArrayList<String>();

        }

        public void addDevice(BluetoothDevice device, String id, int rssi) {
            if(!mLeIds.contains(id))
                mLeIds.add(id);
            ArrayList<Integer> rssiList = mLeDevices.containsKey(id) ? mLeDevices.get(id) : new ArrayList<Integer>();
            rssiList.add(rssi);
            mLeDevices.put(id,rssiList);
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

    private ScanCallback leScanCallBack =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Filtering scan results
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
                                    Log.d(TAG, "run: "+result.getRssi());
                                    Log.d(TAG, "identifier: " + id + " address: " + device.getAddress());
                                    leDeviceList.addDevice(device, id, rssi);
                                }
                            }
                        }
                    });
                }
            };

    private void startMqtt(){
        application.mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {}

            @Override
            public void connectionLost(Throwable throwable) {}

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w(TAG, mqttMessage.toString());
                if (topic.equals(application.S2M_LOOMO_STATUS)) {
                    JSONObject obj = new JSONObject(mqttMessage.toString());
                    String loomoStatus = obj.get("status").toString();

                    if (loomoStatus.equals("available")) {
                        statusTextView.setText(application.LOOMO_AVAILABLE);

                    } else if (loomoStatus.equals("unavailable")) {
                        Toast.makeText(getApplicationContext(), application.LOOMO_UNAVAILABLE, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else if (topic.equals(application.S2M_LOOMO_ARRIVAL)) {
                    MainActivity.setLoomoPresent(true);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else if (topic.equals(application.S2M_ERROR)) {
                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
