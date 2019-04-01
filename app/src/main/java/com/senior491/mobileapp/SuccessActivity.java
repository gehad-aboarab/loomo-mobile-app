package com.senior491.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SuccessActivity extends Activity {

    private TextView successTextView;
    private Button okButton;
    private int destinationMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        successTextView = (TextView) findViewById(R.id.goTextView);
        okButton = (Button) findViewById(R.id.okButton);
        destinationMethod = getIntent().getIntExtra("destinationMethod", 0);

        if(destinationMethod == 0)
            successTextView.setText("Let's go, hop on!");
        else if(destinationMethod == 1)
            successTextView.setText("Let's go, follow me!");

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: send start journey signal to server
                Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", "ongoing journey");
                startActivity(intent);
                finish();
            }
        });
    }
}
