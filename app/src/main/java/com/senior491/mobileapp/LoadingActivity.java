package com.senior491.mobileapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends Activity {

    private String status;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private Button dismissButton;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private App application;
    private String destination;
    private int mode;
    private Timer timer;
    private boolean loomoStatusReceived;
    private ScanningBLE scanningBLE;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private static final String TAG = "SeniorSucks_Loading";

    private ScanningBLE.ServiceInteractionListener mListener = new ScanningBLE.ServiceInteractionListener() {
        @Override
        public void onServiceInteraction(int callBackCode, Object obj) {
            switch(callBackCode){
                case 1001:
                    Toast.makeText(getApplicationContext(),(String)obj,Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case 1002:
                    loomoStatusReceived = false;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(!loomoStatusReceived){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    }, 4000);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        application = (App) getApplication();

        Intent intent = getIntent();
        destination = intent.getStringExtra("destination");
        mode = intent.getIntExtra("mode",application.GUIDE_MODE);
        status = getIntent().getStringExtra("status");

        //GUI initializations
        progressBar = (ProgressBar) findViewById(R.id.loading_progress);
        statusTextView = (TextView) findViewById(R.id.loading_status);
        dismissButton = (Button) findViewById(R.id.loading_dismissLoomo);

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissLoomo(false);
            }
        });
    }

    public void dismissLoomo(boolean serverCommand) {
        if(serverCommand && application.usingLoomo) {
            application.usingLoomo = false;
        } else {
            if(application.usingLoomo){
                MqttMessage msg = new MqttMessage();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("clientID", application.deviceId);
                    obj.put("loomoID", application.loomoId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setPayload(obj.toString().getBytes());
                Log.d(TAG, msg.toString());
                try {
                    application.mqttHelper.mqttAndroidClient.publish(application.M2S_LOOMO_DISMISSAL, msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            } else if(scanningBLE.isScanning()) {
                scanningBLE.stopScan();

            } else {
                MqttMessage msg = new MqttMessage();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("clientID", application.deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setPayload(obj.toString().getBytes());
                Log.d(TAG, msg.toString());
                try {
                    application.mqttHelper.mqttAndroidClient.publish(application.M2S_LOOMO_DISMISSAL, msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
        application.loomoId = null;
        application.usingLoomo = false;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On resume called.");

        //Bluetooth permissions and initializations
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, application.BLE_UNSUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, application.BLUETOOTH_UNSUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        //Location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            if (status.equals("request journey")) {
                statusTextView.setText(application.RETRIEVE_LOCATION);
                progressBar.setVisibility(View.VISIBLE);
                scanningBLE = new ScanningBLE(bluetoothAdapter.getBluetoothLeScanner(), application,mListener, destination, mode);

            } else if (status.equals("ongoing journey")) {
                statusTextView.setText(application.ONGOING_JOURNEY);
                progressBar.setVisibility(View.VISIBLE);
            }

            if (!bluetoothAdapter.isEnabled()) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
            startMqtt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (status.equals("request journey")) {
                        statusTextView.setText(application.RETRIEVE_LOCATION);
                        progressBar.setVisibility(View.VISIBLE);
                        scanningBLE = new ScanningBLE(bluetoothAdapter.getBluetoothLeScanner(), application,mListener, destination, mode);

                    } else if (status.equals("ongoing journey")) {
                        statusTextView.setText(application.ONGOING_JOURNEY);
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
    }

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
                    loomoStatusReceived = true;

                    if (loomoStatus.equals("available")) {
                        statusTextView.setText(application.LOOMO_AVAILABLE);
                        application.loomoId = obj.get("loomoID").toString();

                    } else if (loomoStatus.equals("unavailable")) {
                        Toast.makeText(getApplicationContext(), application.LOOMO_UNAVAILABLE, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else if (topic.equals(application.S2M_LOOMO_ARRIVAL)) {
                    application.usingLoomo = true;
                    Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
                    intent.putExtra("mode", mode);
                    startActivity(intent);

                } else if (topic.equals(application.S2M_LOOMO_DISMISS)) {
                    dismissLoomo(true);

                } else if (topic.equals(application.S2M_ERROR)) {
                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
