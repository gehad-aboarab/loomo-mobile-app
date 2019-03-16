package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private Button callLoomoButton;
    private Spinner destinationSpinner;
    private RadioGroup radioGroup;
    private RadioButton rideRadioButton, guideRadioButton;

    private Intent intent;
    private App application;

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

        if (application.usingLoomo) {
            intent = new Intent(getApplicationContext(), SuccessActivity.class);
            startActivity(intent);
        }

        destinationSpinner = (Spinner) findViewById(R.id.main_destinationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.destination_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(adapter);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.main_rideRadioButton){
                    destinationSpinner.setEnabled(false);
                } else if (checkedId == R.id.main_guideRadioButton){
                    destinationSpinner.setEnabled(true);
                }
            }
        });

        callLoomoButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (v.getId() == R.id.main_callButton) {
                     if(destinationSpinner.getSelectedItem() == null && guideRadioButton.isChecked()){
                         Toast.makeText(getApplicationContext(), "Please select a destination!", Toast.LENGTH_SHORT).show();
                     } else if (guideRadioButton.isChecked()) {
                         String selectedDestination = destinationSpinner.getSelectedItem().toString();
                         intent = new Intent(getApplicationContext(), LoadingActivity.class);
                         intent.putExtra("destination", selectedDestination);
                         intent.putExtra("mode", 0); //guide mode
                         intent.putExtra("status", "request journey");
                         startActivity(intent);
                     } else if(rideRadioButton.isChecked()){
                         intent = new Intent(getApplicationContext(), LoadingActivity.class);
                         intent.putExtra("mode", 1); //ride mode
                         intent.putExtra("status", "request journey");
                         startActivity(intent);
                     }
                 }
             }
        });

//            startMqtt();
        }


//    private void startMqtt(){
//        application.mqttHelper.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean b, String s) {}
//
//            @Override
//            public void connectionLost(Throwable throwable) {}
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w(TAG, mqttMessage.toString());
//
//                if (topic.equals(application.S2M_LOOMO_DISMISSAL)) {
//                    Toast.makeText(getApplicationContext(), application.DISMISSAL_SUCCESSFUL, Toast.LENGTH_SHORT).show();
//                    finish();
//
//                } else if (topic.equals(application.S2M_ERROR)) {
//                    Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//    }

}
