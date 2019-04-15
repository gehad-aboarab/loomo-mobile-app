package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cooltechworks.views.ScratchImageView;
import com.ebanx.swipebtn.OnActiveListener;
import com.ebanx.swipebtn.SwipeButton;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import es.dmoral.toasty.Toasty;

public class MainActivity extends Activity {

//    private Button callLoomoButton;
    private SwipeButton callLoomoButton;
//    private Spinner destinationSpinner, toursSpinner;
    private MaterialSpinner toursSpinner;
    private MaterialSpinner destinationSpinner;
    private RadioGroup radioGroup;
    private RadioButton tourRadioButton, rideRadioButton, guideRadioButton;
    private TextView destinationsLabel, toursLabel;

    private ScratchImageView loomoImage;

    private Intent intent;
    private App application;
    private ArrayList<String> destinationNames;
    private ArrayList<String> tourNames;

    private static final int RETRIEVE_LOCATION = 0;
    private static final String TAG = "SeniorSucks_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (App) getApplication();

        destinationsLabel = (TextView) findViewById(R.id.main_destinationLabel);
        toursLabel = (TextView) findViewById(R.id.main_toursLabel);
        radioGroup = (RadioGroup) findViewById(R.id.main_radioGroup);
        rideRadioButton = (RadioButton) findViewById(R.id.main_rideRadioButton);
        guideRadioButton = (RadioButton) findViewById(R.id.main_guideRadioButton);
        tourRadioButton = (RadioButton) findViewById(R.id.main_tourRadioButton);
        destinationSpinner = (MaterialSpinner) findViewById(R.id.main_destinationSpinner);
        toursSpinner = (MaterialSpinner) findViewById(R.id.main_toursSpinner);
        //callLoomoButton = (Button) findViewById(R.id.main_callButton);
        callLoomoButton = (SwipeButton) findViewById(R.id.main_callButton);
        loomoImage = (ScratchImageView) findViewById(R.id.main_loomoImage);
        loomoImage.setRevealListener(new ScratchImageView.IRevealListener() {
            @Override
            public void onRevealed(ScratchImageView iv) { }
            @Override
            public void onRevealPercentChangedListener(ScratchImageView siv, float percent) {
                if(percent>0.4f){
                    Toasty.success(getApplicationContext(),"You found loomo!!!",Toasty.LENGTH_SHORT,true).show();
                    siv.clear();
                }
            }
        });

        // View the appropriate spinner (or no spinner) based on the selected mode
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.main_rideRadioButton) {
                    destinationsLabel.setVisibility(View.GONE);
                    destinationSpinner.setVisibility(View.GONE);

                    toursLabel.setVisibility(View.GONE);
                    toursSpinner.setVisibility(View.GONE);

                    rideRadioButton.setTextColor(getResources().getColor(R.color.ride_checked));
                    guideRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));
                    tourRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));
                } else if (checkedId == R.id.main_guideRadioButton) {
                    destinationsLabel.setVisibility(View.VISIBLE);
                    destinationSpinner.setVisibility(View.VISIBLE);

                    toursLabel.setVisibility(View.GONE);
                    toursSpinner.setVisibility(View.GONE);

                    rideRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));
                    guideRadioButton.setTextColor(getResources().getColor(R.color.guide_checked));
                    tourRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));

                } else if (checkedId == R.id.main_tourRadioButton) {
                    destinationsLabel.setVisibility(View.GONE);
                    destinationSpinner.setVisibility(View.GONE);

                    toursLabel.setVisibility(View.VISIBLE);
                    toursSpinner.setVisibility(View.VISIBLE);

                    rideRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));
                    guideRadioButton.setTextColor(getResources().getColor(R.color.radio_unchecked));
                    tourRadioButton.setTextColor(getResources().getColor(R.color.tour_checked));
                }
            }
        });

        callLoomoButton.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
                // If user did not select a destination while on guide
                if ((destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()) == null || destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()).equals(application.DEFAULT_DESTINATION))&& guideRadioButton.isChecked()) {
                    Toasty.warning(getApplicationContext(),R.string.error_no_destination_selected,Toast.LENGTH_SHORT,true).show();
                    //Toast.makeText(getApplicationContext(), R.string.error_no_destination_selected, Toast.LENGTH_SHORT).show();

                // If user selected guide
                } else if (guideRadioButton.isChecked()) {
                    String selectedDestination = destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()).toString();
                    Toasty.success(getApplicationContext(),selectedDestination,Toasty.LENGTH_SHORT,true).show();
                    updateApplicationVariables(selectedDestination, null,application.GUIDE_MODE);

                    // Start the next activity
                    intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    startActivity(intent);

                // If user selected ride
                } else if (rideRadioButton.isChecked()) {
                    updateApplicationVariables(null, null,application.RIDE_MODE);

                    // Start the next activity
                    intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    startActivity(intent);

                // If the user did not select a tour while on tour
                } else if ((destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()) == null || destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()).equals(application.DEFAULT_DESTINATION))&& tourRadioButton.isChecked()) {
                    Toasty.warning(getApplicationContext(), R.string.error_no_tour_selected, Toast.LENGTH_SHORT,true).show();
                    //Toast.makeText(getApplicationContext(), R.string.error_no_tour_selected, Toast.LENGTH_SHORT).show();

                // If user selected tour
                } else if(tourRadioButton.isChecked()) {
                    String selectedTour = destinationSpinner.getItems().get(destinationSpinner.getSelectedIndex()).toString();
                    updateApplicationVariables(null, selectedTour, application.TOUR_MODE);

                    // Start the next activity
                    intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    startActivity(intent);
                }
            }
        });

//        callLoomoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v.getId() == R.id.main_callButton) {
//                    // If user did not select a destination while on guide
//                    if ((destinationSpinner.getSelectedItem() == null || destinationSpinner.getSelectedItem().equals(application.DEFAULT_DESTINATION))&& guideRadioButton.isChecked()) {
//                        Toasty.warning(getApplicationContext(),R.string.error_no_destination_selected,Toast.LENGTH_SHORT,true).show();
//                        //Toast.makeText(getApplicationContext(), R.string.error_no_destination_selected, Toast.LENGTH_SHORT).show();
//
//                    // If user selected guide
//                    } else if (guideRadioButton.isChecked()) {
//                        String selectedDestination = destinationSpinner.getSelectedItem().toString();
//                        updateApplicationVariables(selectedDestination, null,application.GUIDE_MODE);
//
//                        // Start the next activity
//                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
//                        startActivity(intent);
//
//                    // If user selected ride
//                    } else if (rideRadioButton.isChecked()) {
//                        updateApplicationVariables(null, null,application.RIDE_MODE);
//
//                        // Start the next activity
//                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
//                        startActivity(intent);
//
//                    // If the user did not select a tour while on tour
//                    } else if ((toursSpinner.getSelectedItem() == null || toursSpinner.getSelectedItem().equals(application.DEFAULT_TOUR))&& tourRadioButton.isChecked()) {
//                        Toasty.warning(getApplicationContext(), R.string.error_no_tour_selected, Toast.LENGTH_SHORT,true).show();
//                        //Toast.makeText(getApplicationContext(), R.string.error_no_tour_selected, Toast.LENGTH_SHORT).show();
//
//                    // If user selected tour
//                    } else if(tourRadioButton.isChecked()) {
//                        String selectedTour = toursSpinner.getSelectedItem().toString();
//                        updateApplicationVariables(null, selectedTour, application.TOUR_MODE);
//
//                        // Start the next activity
//                        intent = new Intent(getApplicationContext(), LoadingActivity.class);
//                        startActivity(intent);
//                    }
//                }
//            }
//        });
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(application.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        application.currentDestination = sp.getString("destination", null);
        application.currentTour = sp.getString("tour", null);
        application.currentMode = sp.getInt("mode", application.GUIDE_MODE);
        application.currentState = sp.getInt("state", application.UNBOUND);
        application.loomoId = sp.getString("loomoId", null);

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
    }

    private void updateApplicationVariables(String destination, String tour, int mode){
        application.updateCurrentDestination(destination);
        application.updateCurrentTour(tour);
        application.updateCurrentMode(mode);
    }

    private void getUpdatedSpinners(String mapName){
        // Preparing message payload
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

        // Requesting tours and destinations from the server
        try {
            application.mqttHelper.mqttAndroidClient.publish(application.M2S_GET_MAP_DESTINATIONS, msg);
            application.mqttHelper.mqttAndroidClient.publish(application.M2S_GET_TOURS, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void initDestinationsSpinner(){
        // Initializing the destinations spinner with the destinations received from server
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

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        destinationSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void initToursSpinner(){
        // Initializing the tours spinner with the tours received from server
        tourNames = new ArrayList<>();
        tourNames.add(application.DEFAULT_TOUR);
        tourNames.addAll(application.tours);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tourNames) {
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

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        toursSpinner.setAdapter(spinnerArrayAdapter);
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
                            // Adding the tour to the array of tours
                            JSONObject tour = obj.getJSONObject("tour");
                            application.tours.clear();
                            application.tours.add(tour.getString("name"));
                            initToursSpinner();
                        } catch(Exception e){
                            Log.d(TAG, "messageArrived Error: "+e.getMessage());
                        }
                    }
                }
            }
        });
    }

}
