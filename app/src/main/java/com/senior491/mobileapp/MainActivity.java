package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        callLoomoButton = (Button) findViewById(R.id.main_callButton);
        destinationSpinner = (Spinner) findViewById(R.id.main_destinationSpinner);
        radioGroup = (RadioGroup) findViewById(R.id.main_radioGroup);
        rideRadioButton = (RadioButton) findViewById(R.id.main_rideRadioButton);
        guideRadioButton = (RadioButton) findViewById(R.id.main_guideRadioButton);
        destinationSpinner = (Spinner) findViewById(R.id.main_destinationSpinner);

        if (application.usingLoomo) {
            intent = new Intent(getApplicationContext(), SuccessActivity.class);
            startActivity(intent);
        }

//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.destination_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        destinationSpinner.setAdapter(adapter);
//        destinationNames = new ArrayList<>();
//        for(Destination d:application.destinations){
//            destinationNames.add(d.getName());
//        }
//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, destinationNames);
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        destinationSpinner.setAdapter(spinnerArrayAdapter);

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
                    if (destinationSpinner.getSelectedItem() == null && guideRadioButton.isChecked()) {
                        Toast.makeText(getApplicationContext(), "Please select a destination!", Toast.LENGTH_SHORT).show();
                    } else if (guideRadioButton.isChecked()) {
                        String selectedDestination = destinationSpinner.getSelectedItem().toString();
                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
                        intent.putExtra("destination", selectedDestination);
                        intent.putExtra("mode", 0); //guide mode
                        intent.putExtra("status", "request journey");
                        startActivity(intent);
                    } else if (rideRadioButton.isChecked()) {
                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
                        intent.putExtra("mode", 1); //ride mode
                        intent.putExtra("status", "request journey");
                        startActivity(intent);
                    }
                }
            }
        });

        if(application.mqttHelper.mqttAndroidClient.isConnected()){
            getUpdatedDestinations("SampleMap");
        } else {
            startMqtt();
        }
    }

    // asking the server for the latest destination names from the map
    private void getUpdatedDestinations(String mapName){
        MqttMessage msg = new MqttMessage();
        JSONObject obj = new JSONObject();
        try {
            obj.put("clientID", application.deviceId);
            obj.put("mapName", application.mapName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msg.setPayload(obj.toString().getBytes());
        Log.d(TAG, msg.toString());
        try {
            application.mqttHelper.mqttAndroidClient.publish(application.M2S_GET_MAP_DESTINATIONS, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Adding the destinations received from the server into the destinations spinner
    private void initDestinations(){
        destinationNames = new ArrayList<>();
        for(Destination d:application.destinations){ destinationNames.add(d.getName()); }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, destinationNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void startMqtt(){
        application.mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) { getUpdatedDestinations(application.mapName); }
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                JSONObject obj = new JSONObject(mqttMessage.toString());
                String clientId = obj.get("clientID").toString();
                if (clientId.equals(application.deviceId)) {
                    if (topic.equals(application.S2M_GET_MAP_DESTINATIONS)) {
                        Log.d(TAG, "inside route"+mqttMessage.toString());
                        JSONArray destinations = obj.getJSONArray("destinations");
                        for (int i = 0; i < destinations.length(); i++) {
                            String name = destinations.getJSONObject(i).getString("name");
                            JSONObject corners = destinations.getJSONObject(i).getJSONObject("corners");
                            String[] cornerArray = new String[]{corners.getString("0"), corners.getString("1"), corners.getString("2"), corners.getString("3")};
                            application.destinations.add(new Destination(name, cornerArray));
                            Log.d(TAG, application.destinations.toString());
                        }

                    }
                    initDestinations();
//                    } else if (topic.equals(mobApp.S2M_ERROR)) {
//                        Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
                }

            }
        });
    }

}
