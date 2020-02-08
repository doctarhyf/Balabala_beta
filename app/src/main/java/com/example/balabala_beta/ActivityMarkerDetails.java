package com.example.balabala_beta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;

public class ActivityMarkerDetails extends AppCompatActivity {

    private static final String TAG = "AMD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);


        String tag = getIntent().getExtras().getString("tag");


        Toolbar toolbar = findViewById(R.id.toolbarMarkerDetails);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("* TITLE *");


        Log.e(TAG, "onCreate: -> Marker Tag : " + tag);


    }
}
