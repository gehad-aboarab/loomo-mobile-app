package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class SuccessActivity extends Activity {

    private String status;
    private TextView successTextView;
    private Button okButton;
    private Button dismissButton;
    private int destinationMethod;
    private App application;
    private final static String TAG = "SeniorSucks_Success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        application = (App) getApplication();
        successTextView = (TextView) findViewById(R.id.success_textView);
        okButton = (Button) findViewById(R.id.success_okButton);
        dismissButton = (Button) findViewById(R.id.success_dismissButton);

        // Get the destination mode from the shared prefs
        SharedPreferences sp = getSharedPreferences(application.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        destinationMethod = sp.getInt("mode", -1);

        // Displaying the appropriate text depending on the mode
        if(destinationMethod == application.RIDE_MODE)
            successTextView.setText("Let's go, hop onto Loomo!");
        else if(destinationMethod == application.GUIDE_MODE)
            successTextView.setText("Let's go, follow Loomo!");
        else {
            successTextView.setText("Journey has ended, you can dismiss Loomo now.");
            okButton.setVisibility(View.GONE);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sending the server the start journey command
                MqttMessage msg = new MqttMessage();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("clientID", application.clientId);
                    obj.put("loomoID", application.loomoId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setPayload(obj.toString().getBytes());
                Log.d(TAG, msg.toString());
                try {
                    application.mqttHelper.mqttAndroidClient.publish(application.M2S_START_JOURNEY, msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissLoomo(false);
            }
        });
    }

    public void dismissLoomo(boolean serverCommand) {
        // If not a server command, inform server to dismiss
        if(!serverCommand) {
                MqttMessage msg = new MqttMessage();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("clientID", application.clientId);
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
        }

        // Update loomoId and current state + shared prefs
        application.loomoId = null;
        application.currentState = application.UNBOUND;
        updateSharedPrefs();

        // Go back to main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMqtt();
    }

    public void updateSharedPrefs() {
        SharedPreferences.Editor editor = getSharedPreferences(application.SHARED_PREF_FILE, Context.MODE_PRIVATE).edit();
        editor.putString("loomoId", application.loomoId);
        editor.putInt("currentState", application.currentState);
        editor.putString("destination", null);
        editor.putInt("mode", -100);
        editor.commit();
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
                JSONObject obj = new JSONObject(mqttMessage.toString());
                if (obj.get("clientID").toString().equals(application.clientId)) {
                    Log.w(TAG, mqttMessage.toString());

                    if (topic.equals(application.S2M_JOURNEY_STARTED)) {
                        // If journey has been started, go to loading activity
                        Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (topic.equals(application.S2M_ERROR)) {
                        Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (topic.equals(application.S2M_LOOMO_DISMISS)) {
                        dismissLoomo(true);
                    }
                }
            }
        });
    }
}
