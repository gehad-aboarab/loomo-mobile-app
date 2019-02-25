package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private Button callLoomoButton, setDestinationButton, dismissLoomoButton;
    private ImageView loomoImage;
    private TextView welcomeTextView;
    private Intent intent;
    private App application;
    private static boolean loomoPresent = false;
    //    private static boolean ongoingJourney = false;
    private static final int RETRIEVE_LOCATION = 0;
    private static final String TAG = "SeniorSucks_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (App) getApplication();

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

        startMqtt();
    }

    class ButtonsListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.callLoomoButton) {
                //Loading activity to retrieve user location and contact server
                intent = new Intent(getApplicationContext(), LoadingActivity.class);
                intent.putExtra("status", RETRIEVE_LOCATION);
                startActivity(intent);

            } else if (view.getId() == R.id.setDestinationButton) {
                //Destination activity to allow user to enter their destination
                intent = new Intent(getApplicationContext(), DestinationActivity.class);
                startActivity(intent);

            } else if (view.getId() == R.id.dismissLoomoButton) {
                //Dismiss Loomo through server
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
                    application.mqttHelper.mqttAndroidClient.publish("mobile-to-server/loomo-dismissal", msg);
                } catch (MqttException e) {
                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void isLoomoPresent() {
        if (loomoPresent) {
            callLoomoButton.setVisibility(View.GONE);
            setDestinationButton.setVisibility(View.VISIBLE);
            dismissLoomoButton.setVisibility(View.VISIBLE);
            loomoImage.setImageResource(R.drawable.loomo_2);
            welcomeTextView.setText("I'm here now.\nWhat would you like to do?");
        } else {
            callLoomoButton.setVisibility(View.VISIBLE);
            setDestinationButton.setVisibility(View.GONE);
            dismissLoomoButton.setVisibility(View.GONE);
            loomoImage.setImageResource(R.drawable.loomo_1);
            welcomeTextView.setText("Hi there! My name is Loomo.\n I can help you find your destination.");
        }
    }

    public static void setLoomoPresent(boolean present){
        loomoPresent = present;
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

                if (topic.equals(application.S2M_LOOMO_DISMISSAL)) {
                    Toast.makeText(getApplicationContext(), application.DISMISSAL_SUCCESSFUL, Toast.LENGTH_SHORT).show();
                    finish();

                } else if (topic.equals(application.S2M_ERROR)) {
                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}
