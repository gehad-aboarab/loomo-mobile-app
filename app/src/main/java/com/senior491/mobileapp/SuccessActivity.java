package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SuccessActivity extends Activity {

    private TextView successTextView;
    private Button okButton;
    private int destinationMethod;
    private App application;
    private final static String TAG = "SeniorSucks_Success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        application = (App) getApplication();
        successTextView = (TextView) findViewById(R.id.goTextView);
        okButton = (Button) findViewById(R.id.okButton);
        destinationMethod = getIntent().getIntExtra("mode", application.GUIDE_MODE);

        if(destinationMethod == application.RIDE_MODE)
            successTextView.setText("Let's go, hop on!");
        else if(destinationMethod == application.GUIDE_MODE)
            successTextView.setText("Let's go, follow me!");

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    application.mqttHelper.mqttAndroidClient.publish(application.M2S_START_JOURNEY, msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", "ongoing journey");
                startActivity(intent);
                finish();
            }
        });
    }
}
