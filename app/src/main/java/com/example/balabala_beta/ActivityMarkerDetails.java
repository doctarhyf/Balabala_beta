package com.example.balabala_beta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivityMarkerDetails extends AppCompatActivity {

    private static final String TAG = "AMD";
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference insecAudioRef = null;
    private MediaPlayer player = null;
    private String mInsecAudioFileName = null;
    private File mLocalFile = null;
    Button btnRePlayInsecAudio = null;
    ProgressBar progress = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);

        progress = findViewById(R.id.pbInsecAudio);

        Toolbar toolbar = findViewById(R.id.toolbarMarkerDetails);
        setSupportActionBar(toolbar);


        btnRePlayInsecAudio = findViewById(R.id.btnRePlayInsecAudio);
        btnRePlayInsecAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playInsecAudio();
            }
        });


        mInsecAudioFileName = getIntent().getExtras().getString("tag");

        getSupportActionBar().setTitle(mInsecAudioFileName);


        Log.e(TAG, "onCreate: -> da ref : " + "insec_audio/" + mInsecAudioFileName );

        insecAudioRef = storageRef.child("insec_audio/" + mInsecAudioFileName);

        mLocalFile = null;
        try {
            mLocalFile = File.createTempFile(mInsecAudioFileName, "3pg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        insecAudioRef.getFile(mLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.e(TAG, "onSuccess: -> downloaded : " + mInsecAudioFileName );
                playInsecAudio();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e(TAG, "onFailure: -> " + exception.toString() );
            }
        });



    }



    private void playInsecAudio() {
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        try {


            String file = mLocalFile.getAbsolutePath();

            Log.e(TAG, "playInsecAudio: file -> " + file );
            player.setDataSource(file);//(mLocalFile);
            player.prepare();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                progress.setMin(0);
            }
            progress.setMax(player.getDuration());
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    progress.setProgress(player.getCurrentPosition());
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

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {


                progress.setProgress(player.getCurrentPosition());
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
