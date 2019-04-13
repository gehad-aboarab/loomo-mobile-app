package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.FontsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import java.util.Collections;

public class MainActivity extends Activity {

    private Button callLoomoButton;
    private Spinner destinationSpinner;
    private RadioGroup radioGroup;
    private RadioButton rideRadioButton, guideRadioButton;

    private Intent intent;
    private App application;
    private ArrayList<String> destinationNames;

    private static final int RETRIEVE_LOCATION = 0;
    private static final String TAG = "SeniorSucks_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (App) getApplication();

        radioGroup = (RadioGroup) findViewById(R.id.main_radioGroup);
        rideRadioButton = (RadioButton) findViewById(R.id.main_rideRadioButton);
        guideRadioButton = (RadioButton) findViewById(R.id.main_guideRadioButton);
        destinationSpinner = (Spinner) findViewById(R.id.main_destinationSpinner);
        callLoomoButton = (Button) findViewById(R.id.main_callButton);

        // Start respective activity based on state
        if (application.currentState == application.BOUND_WAITING) {
            intent = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(intent);
        } else if (application.currentState == application.BOUND_JOURNEY_STARTABLE) {
            intent = new Intent(getApplicationContext(), SuccessActivity.class);
            startActivity(intent);
        } else if (application.currentState == application.BOUND_ONGOING_JOURNEY) {
            intent = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(intent);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.main_rideRadioButton) {
                    destinationSpinner.setEnabled(false);
                } else if (checkedId == R.id.main_guideRadioButton) {
                    destinationSpinner.setEnabled(true);
                }
            }
        });

        callLoomoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.main_callButton) {
                    // If user did not select a destination while on guide
                    if ((destinationSpinner.getSelectedItem() == null || destinationSpinner.getSelectedItem().equals(application.DEFAULT_DESTINATION))&& guideRadioButton.isChecked()) {
                        Toast.makeText(getApplicationContext(), R.string.error_no_destination_selected, Toast.LENGTH_SHORT).show();

                    // If user selected guide
                    } else if (guideRadioButton.isChecked()) {
                        String selectedDestination = destinationSpinner.getSelectedItem().toString();

                        // Store the destination and mode
                        SharedPreferences.Editor editor = getSharedPreferences(application.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
                        editor.putString("destination", selectedDestination);
                        editor.putInt("mode", application.GUIDE_MODE);
                        editor.commit();

                        // Start the next activity
                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
                        startActivity(intent);

                    // If user selected ride
                    } else if (rideRadioButton.isChecked()) {
                        // Store the mode
                        SharedPreferences.Editor editor = getSharedPreferences(application.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
                        editor.putInt("mode", application.RIDE_MODE);
                        editor.commit();

                        // Start the next activity
                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve the updated destinations from the map
        application.destinations.clear();
        if(application.mqttHelper.mqttAndroidClient.isConnected()){
            getUpdatedSpinners(application.mapName);
        }
        startMqtt();
    }

    private void getUpdatedSpinners(String mapName){
        // Ask the server to send the updated map
        MqttMessage msg = new MqttMessage();
        JSONObject obj = new JSONObject();
        try {
            obj.put("clientID", application.clientId);
            obj.put("mapName", application.mapName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msg.setPayload(obj.toString().getBytes());
        Log.d(TAG, msg.toString());
        try {
            // getting tours and destinations
            application.mqttHelper.mqttAndroidClient.publish(application.M2S_GET_MAP_DESTINATIONS, msg);
            application.mqttHelper.mqttAndroidClient.publish(application.M2S_GET_TOURS, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void initDestinationsSpinner(){
        // Initializing the spinner with the destinations received from server
        destinationNames = new ArrayList<>();
        destinationNames.add(application.DEFAULT_DESTINATION);
        destinationNames.addAll(application.destinations);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, destinationNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.NORMAL));
                textView.setTextColor(Color.WHITE);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getDropDownView(position, convertView, parent);
            }
        };
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        destinationSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void initToursSpinner(){
        //TODO Tours spinner logic here
    }

    private void startMqtt(){
        application.mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) { getUpdatedSpinners(application.mapName); }
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                JSONObject obj = new JSONObject(mqttMessage.toString());
                String clientId = obj.get("clientID").toString();
                if (clientId.equals(application.clientId)) {
                    Log.w(TAG, mqttMessage.toString());

                    if (topic.equals(application.S2M_GET_MAP_DESTINATIONS)) {
                        try {
                            JSONArray destinations = obj.getJSONArray("destinations");
                            application.destinations.clear();

                            // Adding the destinations to the array of destinations
                            for (int i = 0; i < destinations.length(); i++) {
                                String name = destinations.getJSONObject(i).getString("name");
                                application.destinations.add(name);
                            }
                            Collections.sort(application.destinations, String.CASE_INSENSITIVE_ORDER);
                            initDestinationsSpinner();

                        } catch(Exception e){
                            Log.d(TAG, "messageArrived Error: "+e.getMessage());
                        }
                    } else if(topic.equals(application.S2M_GET_TOURS)) {
                        try {
                            JSONObject tour = obj.getJSONObject("tour");
                            application.tours.clear();
                            application.tours.add(tour.getString("tourName"));
                            initToursSpinner();
                        } catch(Exception e){
                            // No tours available
                            Log.d(TAG, "messageArrived Error: "+e.getMessage());
                        }
                    }
                }
            }
        });
    }

}
