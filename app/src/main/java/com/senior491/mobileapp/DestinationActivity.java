package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.net.HttpURLConnection;

public class DestinationActivity extends Activity {

    private EditText destinationEditText;
    private Button submitDestinationButton;
    private RadioGroup radioGroup;
    private RadioButton rideRadioButton, guideRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        destinationEditText = (EditText) findViewById(R.id.destinationEditText);
        submitDestinationButton = (Button) findViewById(R.id.submitDestinationButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rideRadioButton = (RadioButton) findViewById(R.id.rideRadioButton);
        guideRadioButton = (RadioButton) findViewById(R.id.guideRadioButton);

        submitDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.submitDestinationButton) {
                    //Run async task to send the destination to the server
                    new SubmitDestinationTask().execute();
                }
            }
        });
    }

    class SubmitDestinationTask extends AsyncTask<Void, Void, Boolean> {

        private int response = 200;
        private String status;
        private Intent intent;

        private int destinationMethod;
        private String destination;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(rideRadioButton.isChecked())
                destinationMethod = 0;
            else
                destinationMethod = 1;

            destination = destinationEditText.getText().toString();
            intent = new Intent(getApplicationContext(), SuccessActivity.class);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String servicePath = "/set-destination/" + destinationMethod + "?destination=" + destination;
            //TODO: code to send destination to server

            if(response != HttpURLConnection.HTTP_OK)
                return false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if(success) {
                intent.putExtra("destinationMethod", destinationMethod);
                startActivity(intent);
            } else {
                //TODO: check response
            }

        }
    }
}
