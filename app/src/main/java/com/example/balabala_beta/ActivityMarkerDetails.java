package com.example.balabala_beta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.balabala_beta.dummy.RoadBlocks;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivityMarkerDetails extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "AMD";
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference insecAudioRef = null;
    private MediaPlayer player = null;
    private String mInsecAudioFileName = null;
    private File mLocalFile = null;
    Button btnRePlayInsecAudio = null;
    ProgressBar progress = null;
    FirebaseUser user;
    TextView tvUser = null;
    TextView tvDateTime = null;
    private int mInsecAudioTotalDuration = 0;
    TextView tvLoadAudioFromServerMessage = null;
    private boolean insecAudioLoaded = false;
    private String mInsecAudioFileSenderEmail = "no_email";
    private GoogleMap mMap;
    private MapView mMapView;
    private Bundle mapViewBundle;
    private String MAPVIEW_BUNDLE_KEY = "mvbk";
    private LatLng mLatLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }



        tvLoadAudioFromServerMessage = findViewById(R.id.tvLoadAudioFromServerMessage);

        user = FirebaseAuth.getInstance().getCurrentUser();

        tvUser = findViewById(R.id.textViewUser);
        tvDateTime = findViewById(R.id.textViewDateTime);




        progress = findViewById(R.id.pbInsecAudio);

        Toolbar toolbar = findViewById(R.id.toolbarMarkerDetails);
        setSupportActionBar(toolbar);


        btnRePlayInsecAudio = findViewById(R.id.btnRePlayInsecAudio);
        btnRePlayInsecAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rePlayInsecAudio();
            }
        });


        String[] tag = getIntent().getExtras().getString("tag").split("\uD83D\uDE21");
        mInsecAudioFileName = tag[0];
        mInsecAudioFileSenderEmail = tag[1];
        mLatLng = new LatLng(Double.parseDouble(tag[2]), Double.parseDouble(tag[3]));
        tvUser.setText("Signale par : " + mInsecAudioFileSenderEmail);

        //Log.e(TAG, "onCreate: DA CHAK : -> " + mInsecAudioFileName);

        getSupportActionBar().setTitle("Audio de : " + mInsecAudioFileSenderEmail);


        Log.e(TAG, "onCreate: -> da ref : " + "insec_audio/" + mInsecAudioFileName );

        insecAudioRef = storageRef.child("insec_audio/" + mInsecAudioFileName);

        mLocalFile = null;
        try {
            mLocalFile = File.createTempFile(mInsecAudioFileName, "3pg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        insecAudioRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
                tvDateTime.setText("Date et heure : " + Utils.FormatMillisToDateTime(storageMetadata.getCreationTimeMillis(), null));
            }
        });

        insecAudioRef.getFile(mLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.e(TAG, "onSuccess: -> downloaded : " + mInsecAudioFileName );

                insecAudioLoaded = true;
                toggleReplayInsecAudioBtn();
                playInsecAudio();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e(TAG, "onFailure: -> " + exception.toString() );
                insecAudioLoaded = false;
                toggleReplayInsecAudioBtn();
            }
        });




        toggleSenderINfoTextViews(false);

        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = findViewById(R.id.markerDetMap);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        Button subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Subscribing to balabala topic");
                // [START subscribe_topics]

                FirebaseMessaging.getInstance().subscribeToTopic("balabala")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = getString(R.string.msg_subscribed);
                                if (!task.isSuccessful()) {
                                    msg = getString(R.string.msg_subscribe_failed);
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(ActivityMarkerDetails.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                // [END subscribe_topics]
            }
        });

        Button logTokenButton = findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                // [START retrieve_current_token]
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.e(TAG, "DATOK : " + token);
                                Log.d(TAG, msg);
                                Toast.makeText(ActivityMarkerDetails.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                // [END retrieve_current_token]
            }
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void toggleReplayInsecAudioBtn() {
        btnRePlayInsecAudio.setEnabled(insecAudioLoaded);
    }


    @Override
    public void onBackPressed() {

       if(insecAudioLoaded){
           stopPlayingInsecAudio();
           super.onBackPressed();

       }

        return;
    }

    private void stopPlayingInsecAudio() {
        if(player != null ){
            player.stop();
            //player.release();
            //player = null;
        }
    }

    private void rePlayInsecAudio() {

        if(player != null) {
            player.seekTo(0);
            player.start();
        }else{
            playInsecAudio();
        }

    }

    private void playInsecAudio() {

        toggleSenderINfoTextViews(true);
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        player = new MediaPlayer();
        try {


            String file = mLocalFile.getAbsolutePath();

            Log.e(TAG, "playInsecAudio: file -> " + file);
            player.setDataSource(file);//(mLocalFile);
            player.prepare();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                progress.setMin(0);
            }

            mInsecAudioTotalDuration = player.getDuration();
            progress.setMax(mInsecAudioTotalDuration);
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    progress.setProgress(mInsecAudioTotalDuration);
                    player.release();
                    player = null;
                    observer.stop();


                }
            });

            observer = new MediaObserver();
            //mediaPlayer.start();
            new Thread(observer).start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

    }

    private void toggleSenderINfoTextViews(boolean show) {
        if(show){

            tvLoadAudioFromServerMessage.setVisibility(View.INVISIBLE);
            tvUser.setVisibility(View.VISIBLE);
            tvDateTime.setVisibility(View.VISIBLE);

        }else{
            tvLoadAudioFromServerMessage.setVisibility(View.VISIBLE);
            tvUser.setVisibility(View.INVISIBLE);
            tvDateTime.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: map ready " );
        mMap = googleMap;

        if(mLatLng != null){

            int rbIcon = R.drawable.rb_insec;
            RoadBlocks.GetDummyRoadBlockIconFromFirebaseDBRBType(ActivityMarkerDetails.this, R.drawable.rb_insec);//rb.getRoadBlockIdx());
            Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(rbIcon)).position(mLatLng));

            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    mLatLng, Utils.FOLLOW_ME_ZOOM_LEVEL);
            mMap.animateCamera(location);
        }

    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {


                //Log.e(TAG, "run: efin runnin" );

                if(player != null) {
                    progress.setProgress(player.getCurrentPosition());
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private MediaObserver observer = null;


}
