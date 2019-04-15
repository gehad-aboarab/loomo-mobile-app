package com.senior491.mobileapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

public class Main2Activity extends Activity {

    private Shimmer shimmer;
    private ShimmerTextView shimmerTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        shimmerTextView = (ShimmerTextView)findViewById(R.id.shimmer_tv);
        shimmer = new Shimmer();
        shimmer.start(shimmerTextView);
    }
}
