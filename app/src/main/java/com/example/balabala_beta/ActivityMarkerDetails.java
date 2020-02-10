package com.example.balabala_beta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ActivityMarkerDetails extends AppCompatActivity {

    private static final String TAG = "AMD";
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference insecAudioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);

        Toolbar toolbar = findViewById(R.id.toolbarMarkerDetails);
        setSupportActionBar(toolbar);



        String tag = getIntent().getExtras().getString("tag");

        getSupportActionBar().setTitle(tag);


        //insecAudioRef = storageRef.child("insec_audio/" + tag);
        //Log.e(TAG, "onCreate: -> " + insecAudioRef.toString() );
        Log.e(TAG, "onCreate: -> da ref : " + "insec_audio/" + tag );

    }
}
