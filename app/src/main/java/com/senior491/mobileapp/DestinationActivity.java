package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DestinationActivity extends Activity {

    private EditText destinationEditText;
    private Button submitDestinationButton;
    private RadioGroup radioGroup;
    private RadioButton rideRadioButton, guideRadioButton;
    private App application;

    private static final String TAG = "SeniorSucks_Destination";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        application = (App) getApplication();

        destinationEditText = (EditText) findViewById(R.id.destinationEditText);
        submitDestinationButton = (Button) findViewById(R.id.submitDestinationButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rideRadioButton = (RadioButton) findViewById(R.id.rideRadioButton);
        guideRadioButton = (RadioButton) findViewById(R.id.guideRadioButton);

        submitDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.submitDestinationButton) {
                    //Send the destination to the server
                    MqttMessage msg = new MqttMessage();
                    JSONObject obj = new JSONObject();

                    String destination = destinationEditText.getText().toString();
                    String destinationMethod;
                    if(rideRadioButton.isChecked())
                        destinationMethod = "ride";
                    else
                        destinationMethod = "guide";

                    try {
                        obj.put("destination", destination);
                        obj.put("mode", destinationMethod);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    msg.setPayload(obj.toString().getBytes());
                    Log.d(TAG, msg.toString());
                    try {
                        application.mqttHelper.mqttAndroidClient.publish(application.M2S_USER_DESTINATION, msg);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        startMqtt();
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
                if (topic.equals(application.S2M_USER_DESTINATION)) {
                    Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
                    startActivity(intent);

                } else if (topic.equals(application.S2M_ERROR)) {
                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
