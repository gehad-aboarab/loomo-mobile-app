package com.senior491.mobileapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
//import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
//import com.estimote.proximity_sdk.api.ProximityObserver;
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
//import com.estimote.proximity_sdk.api.ProximityZone;
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
//import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainActivity extends Activity {

    private Button callLoomoButton, setDestinationButton, dismissLoomoButton;
    private ImageView loomoImage;
    private TextView welcomeTextView;
    private static boolean loomoPresent = false;
    private static boolean ongoingJourney = false;
    private Intent intent;

    private BluetoothAdapter mBluetoothAdapter;
    // Initializes Bluetooth adapter.
    final BluetoothManager bluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

    //private ProximityObserver proximityObserver;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.d("app", "name: " + deviceName + " -  address: " + deviceHardwareAddress);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeTextView = (TextView) findViewById(R.id.loomoWelcome);
        callLoomoButton = (Button) findViewById(R.id.callLoomoButton);
        setDestinationButton = (Button) findViewById(R.id.setDestinationButton);
        dismissLoomoButton = (Button) findViewById(R.id.dismissLoomoButton);
        loomoImage = (ImageView) findViewById(R.id.loomoImage);

        isLoomoPresent();

        ButtonsListener listener = new ButtonsListener();

        callLoomoButton.setOnClickListener(listener);
        setDestinationButton.setOnClickListener(listener);
        dismissLoomoButton.setOnClickListener(listener);

//        EstimoteCloudCredentials cloudCredentials =
//                new EstimoteCloudCredentials("loomo-senior-design--656", "f208d315573b45847581bfc94a28f60d");

//        this.proximityObserver =
//                new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
//                        .onError(new Function1<Throwable, Unit>() {
//                            @Override
//                            public Unit invoke(Throwable throwable) {
//                                Log.e("app", "proximity observer error: " + throwable);
//                                return null;
//                            }
//                        })
//                        .withBalancedPowerMode()
//                        .build();
//
//
//
//        final ProximityZone zone = new ProximityZoneBuilder()
//                .forTag("desk")
//                .inCustomRange(0.4)
//                .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
//                    @Override
//                    public Unit invoke(Set<? extends ProximityZoneContext> contexts) {
//                        List<String> deskOwners = new ArrayList<>();
//                        for (ProximityZoneContext context : contexts) {
//                            deskOwners.add(context.getAttachments().get("desk-owner"));
//                        }
//                        Log.d("app", "In range of desks: " + deskOwners);
//                        return null;
//                    }
//                })
//                .onEnter(new Function1<ProximityZoneContext, Unit>() {
//                    @Override
//                    public Unit invoke(ProximityZoneContext context) {
//                        String deskOwner = context.getAttachments().get("desk-owner");
//                        String beaconId = context.getDeviceId();
//                        Log.d("app", context.toString());
//                        Log.d("app", "Welcome to " + deskOwner + ", Beacon ID: " + beaconId);
//                        return null;
//                    }
//                })
//                .onExit(new Function1<ProximityZoneContext, Unit>() {
//                    @Override
//                    public Unit invoke(ProximityZoneContext context) {
//                        String deskOwner = context.getAttachments().get("desk-owner");
//                        Log.d("app", "Bye bye, come again to " + deskOwner + "!");
//                        return null;
//                    }
//                })
//                .build();
//
//        RequirementsWizardFactory
//                .createEstimoteRequirementsWizard()
//                .fulfillRequirements(this,
//                        // onRequirementsFulfilled
//                        new Function0<Unit>() {
//                            @Override public Unit invoke() {
//                                Log.d("app", "requirements fulfilled");
//                                proximityObserver.startObserving(zone);
//                                return null;
//                            }
//                        },
//                        // onRequirementsMissing
//                        new Function1<List<? extends Requirement>, Unit>() {
//                            @Override public Unit invoke(List<? extends Requirement> requirements) {
//                                Log.e("app", "requirements missing: " + requirements);
//                                return null;
//                            }
//                        },
//                        // onError
//                        new Function1<Throwable, Unit>() {
//                            @Override public Unit invoke(Throwable throwable) {
//                                Log.e("app", "requirements error: " + throwable);
//                                return null;
//                            }
//                        });

        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    void isLoomoPresent() {
        if (loomoPresent) {
            callLoomoButton.setVisibility(View.GONE);
            setDestinationButton.setVisibility(View.VISIBLE);
            loomoImage.setImageResource(R.drawable.loomo_2);
            welcomeTextView.setText("I'm here now.\nWhat would you like to do?");
        } else {
            callLoomoButton.setVisibility(View.VISIBLE);
            setDestinationButton.setVisibility(View.GONE);
            loomoImage.setImageResource(R.drawable.loomo_1);
            welcomeTextView.setText("Hi there! My name is Loomo.\n What would you like to do?");
        }
    }

    class ButtonsListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.callLoomoButton) {
                //Run async task to find location of user
//                new LocateUserTask().execute();
//                Run asyn task to send server user's location
                new SendLocationTask().execute();

            } else if (view.getId() == R.id.setDestinationButton) {
                intent = new Intent(getApplicationContext(), DestinationActivity.class);
                startActivity(intent);

            } else if (view.getId() == R.id.dismissLoomoButton) {
                //Run async task to let server know Loomo is no longer needed
                new DismissLoomoTask().execute();

            }
        }
    }

//    class LocateUserTask extends AsyncTask<String, String, String> {
//
//        private String status;
//        private boolean success = true;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            status = "Retrieving your location..";
//            intent = new Intent(getApplicationContext(), LoadingActivity.class);
//            intent.putExtra("status", status);
//            startActivity(intent);
//
//
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            //TODO: code to obtain and store beacon IDs and signals
//
//            //simulate finding the location
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (success)
//                return "Success";
//            return "Failure";
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//
//            if (s.equals("Success")) {
//                //Run async task to contact the server
//                new SendLocationTask().execute();
//            } else {
//                status = "Cannot get your location at the moment\nPlease try again later.";
//                LoadingActivity.statusTextView.setText(status);
//                LoadingActivity.progressBar.setVisibility(View.GONE);
//            }
//        }
//    }

    class SendLocationTask extends AsyncTask<String, String, String> {

        private String status;
        private boolean success = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //TODO: check if user's location is reachable, if so load activity
            intent = new Intent(getApplicationContext(), LoadingActivity.class);
            intent.putExtra("status", status);
            startActivity(intent);
            status = "Please wait for me\n I am on my way to you..";
            LoadingActivity.statusTextView.setText(status);

            //TODO: if user's location is unreachable, display error
        }

        @Override
        protected String doInBackground(String... strings) {
            //TODO: code to send server the user's location

            //simulate loomo on its way to user's location
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (success)
                return "Success";
            return "Failure";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("Success")) {
                loomoPresent = true;
                isLoomoPresent();
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                status = "Server error has occurred\nPlease try again later.";
                LoadingActivity.statusTextView.setText(status);
                LoadingActivity.progressBar.setVisibility(View.GONE);
            }
        }
    }

    class DismissLoomoTask extends AsyncTask<Void, Void, Boolean> {

        private int response = 200;
        private String status;

        @Override
        protected Boolean doInBackground(Void... voids) {
            String servicePath = "/dismiss/";
            //TODO: code to communicate with server

            if (response != HttpURLConnection.HTTP_OK)
                return false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                loomoPresent = false;
                isLoomoPresent();
                finish();
            } else {
                status = "Server error has occurred\nPlease try again later.";
                LoadingActivity.statusTextView.setText(status);
                LoadingActivity.progressBar.setVisibility(View.GONE);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
