package com.senior491.mobileapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ScanningBLE {

    private Handler handler;
    private LeDeviceList leDeviceList;
    private BluetoothLeScanner bluetoothScanner;
    private boolean mScanning;
    private App application;
    private ServiceInteractionListener mListener;
    private String mode;
    private boolean scanningStopped;

    private static final String TAG = "ScanningBLE_Tag";
    private static final long SCAN_PERIOD = 5000;

    public ScanningBLE(BluetoothLeScanner scanner, App application, ServiceInteractionListener mListener){
        bluetoothScanner = scanner;
        this.application = application;
        handler = new Handler();
        leDeviceList = new LeDeviceList();
        this.mListener = mListener;

        if(application.currentMode == application.GUIDE_MODE) {
            this.mode = "guide";
        } else if(application.currentMode == application.RIDE_MODE){
            this.mode = "ride";
        } else if(application.currentMode == application.TOUR_MODE){
            this.mode = "tour";
        }
        scanningStopped = false;
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "Scan method called.");
        if (enable) {
            //Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mScanning) {
                        mScanning = false;
                        validLocation();
                        bluetoothScanner.stopScan(leScanCallBack);
                        Log.d(TAG, "Scanning stopped by itself.");
                    }
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothScanner.startScan(leScanCallBack);
        }
    }

    public void stopScan(){
        mScanning = false;
        scanningStopped = true;
        Log.d(TAG, "Scanning stopped by user.");
        bluetoothScanner.stopScan(leScanCallBack);
    }

    public boolean isScanning(){
        return mScanning;
    }

    public void validLocation(){
        if (leDeviceList.getCount() >= 1) {
            //Connection to server
            MqttMessage msg = new MqttMessage();
            JSONObject obj = new JSONObject();
            try {
                JSONArray signalsArray = new JSONArray();

                for(int i=0; i<leDeviceList.getCount(); i++) {
                    JSONObject signals = new JSONObject();
                    signals.put(leDeviceList.mLeIds.get(i), leDeviceList.mLeDevices.get(leDeviceList.mLeIds.get(i)));
                    signalsArray.put(signals);
                }

                obj.put("clientID", application.clientId);
                obj.put("beaconSignals", signalsArray);
                obj.put("mapName", application.mapName);
                obj.put("destination", application.currentDestination);
                obj.put("tour", application.currentTour);
                obj.put("mode", mode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg.setPayload(obj.toString().getBytes());
            Log.d(TAG, msg.toString());
            try {
                Log.d(TAG, "Sending hussain stuff now!");
                application.mqttHelper.mqttAndroidClient.publish(application.M2S_BEACON_SIGNALS, msg);
                mListener.onServiceInteraction(1002, "");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else {
            if(mListener!=null && !scanningStopped)
                mListener.onServiceInteraction(1001, application.CANNOT_LOCATE);
        }
    }

    private class LeDeviceList {
        private ArrayList<BluetoothDevice> mLeDevice;
        private ArrayList<Integer> mLeRssis;
        private ArrayList<String> mLeIds;
        private HashMap<String, ArrayList<Integer>> mLeDevices;

        public LeDeviceList() {
            super();
            mLeDevices = new HashMap<>();
            mLeRssis = new ArrayList<Integer>();
            mLeIds = new ArrayList<String>();

        }

        public void addDevice(BluetoothDevice device, String id, int rssi) {
            Log.d(TAG, "addDevice: "+id+" "+rssi);
            if(!mLeIds.contains(id))
                mLeIds.add(id);
            ArrayList<Integer> rssiList = mLeDevices.containsKey(id) ? mLeDevices.get(id) : new ArrayList<Integer>();
            rssiList.add(rssi);
            mLeDevices.put(id,rssiList);
            Log.d(TAG, "addDevice: "+ mLeDevices.toString());
        }

        public int getRssi(int position) {
            return mLeRssis.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            mLeRssis.clear();
            mLeIds.clear();
        }

        public int getCount() {
            return mLeIds.size();
        }
    }

    private ScanCallback leScanCallBack =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    //Filtering scan results
                    ArrayList<String> beacons = new ArrayList<String>();

                    beacons.add("59bfdda585767280f886db284653ee35"); //Icy B
                    beacons.add("283acdcf5be28c0f71dc4b6a84219d29"); //Icy A
                    beacons.add("5812ca89ff64bf356564f5ee641f6f1b"); //Mint B
                    beacons.add("6a811095d963f29290ea5371b4177020"); //Mint A
                    beacons.add("3c52a5930c34db229451868164d7fc13"); //Coconut B
                    beacons.add("4454649ebee76a8e5f23a202825c8401"); //Coconut A
                    beacons.add("e158516ea666f214c38d5464c5440d1f"); //Blueberry B
                    beacons.add("d9b0b6f879088d8f767576e07841e43a"); //Blueberry A

                    byte[] bytes = result.getScanRecord().getServiceData(ParcelUuid.fromString("0000fe9a-0000-1000-8000-00805f9b34fb"));
                    if(bytes != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < bytes.length; i++) {
                            if (i <= 16)
                                sb.append(String.format("%02x", bytes[i]));
                        }
                        if (beacons.contains(sb.toString())) {
                            BluetoothDevice device = result.getDevice();
                            String id = sb.toString();
                            int rssi = result.getRssi();
                            Log.d(TAG, "run: " + result.getRssi());
                            Log.d(TAG, "identifier: " + id + " address: " + device.getAddress());
                            leDeviceList.addDevice(device, id, rssi);
                        }
                    }
                }
            };

    public interface ServiceInteractionListener{
        void onServiceInteraction(int callBackCode, Object obj);
    }
}
