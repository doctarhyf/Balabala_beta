package com.example.balabala_beta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class ActivityMarkerDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);


        Bundle data = getIntent().getExtras();


        Toolbar toolbar = findViewById(R.id.toolbarMarkerDetails);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(data.getString("title"));





    }
}
