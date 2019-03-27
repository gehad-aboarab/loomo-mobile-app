package com.senior491.mobileapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;

    final String serverUri = "tcp://m24.cloudmqtt.com:17852";
    final String clientId = "ExampleAndroidClient";
    final String subscriptionTopic = "server-to-mobile/#";
    final String username = "gwvgvrbb";
    final String password = "ZaQHr9ysNDPm";
    private final String TAG = "SeniorSucks_Mqtt";
    final App mobApp;

    public MqttHelper(Application app) {

        Context context = app.getApplicationContext();
        mobApp = (App) app;
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w(TAG, s);

//                MqttMessage msg = new MqttMessage();
//                JSONObject obj = new JSONObject();
//                try {
//                    obj.put("clientID", mobApp.deviceId);
//                    obj.put("mapName", "SampleMap");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                msg.setPayload(obj.toString().getBytes());
//                Log.d(TAG, msg.toString());
//                try {
//                    mqttAndroidClient.publish(mobApp.M2S_GET_MAP_DESTINATIONS, msg);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }

            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.d(TAG, "connectionLost: i have been lost died ");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w(TAG, mqttMessage.toString());
                JSONObject obj = new JSONObject(mqttMessage.toString());
                String clientId = obj.get("clientID").toString();
                if (clientId.equals(mobApp.deviceId)) {
                    if (topic.equals(mobApp.S2M_GET_MAP_DESTINATIONS)) {
                        JSONArray destinations = obj.getJSONArray("destinations");
                        for (int i = 0; i < destinations.length(); i++) {
                            String name = destinations.getJSONObject(i).getString("name");
                            JSONObject corners = destinations.getJSONObject(i).getJSONObject("corners");
                            String[] cornerArray = new String[]{corners.getString("0"), corners.getString("1"), corners.getString("2"), corners.getString("3")};
                            mobApp.destinations.add(new Destination(name, cornerArray));
                            Log.d(TAG, mobApp.destinations.toString());
                        }

                    }
//                    } else if (topic.equals(mobApp.S2M_ERROR)) {
//                        Toast.makeText(getApplicationContext(), application.SERVER_ERROR, Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Failed to connect to: " + serverUri + exception.toString());
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }
}