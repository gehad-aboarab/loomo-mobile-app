package com.senior491.mobileapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingActivity extends Activity {

    private String status;
    public static ProgressBar progressBar;
    public static TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        status = getIntent().getStringExtra("status");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText(status);
    }
}
