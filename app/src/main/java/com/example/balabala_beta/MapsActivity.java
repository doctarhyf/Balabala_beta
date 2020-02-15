package com.example.balabala_beta;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.balabala_beta.dummy.RoadBlocks;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final long MS_DURATION_REFRESH_CURRENT_LOCATION = 5000;
    private static final float MAP_DEFAULT_BLOCK_ZOOM_LEVEL = 20f;
    //private static final int DEFAULT_ROADBLOCK_ID = 0;
    private static final int NUM_DEF_ROAD_BLOCKS = 3;
    private static final float FOLLOW_ME_ZOOM_LEVEL = 18;
    private static final int INSECURITY_AUDIO_RECORD_TIME_SEC = 10;
    private static final String LOG_TAG = "MAP_ACT";
    private boolean firstShot = true;
    private GoogleMap mMap;

    private static final String TAG = "balabala";

    private static final LatLng RHYF = new LatLng(-11.629749, 27.488710);
    private static final LatLng P1 = new LatLng(-11.625700, 27.485300);
    //private static final LatLng P2 = new LatLng(-11.620, 27.480);


    private AppBarConfiguration mAppBarConfiguration;
    private LocationManager mLocationManager;

    // GPSTracker class
    GPSTracker gps;
    private Timer myTimer;

    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    private Marker curLocationMarker;
    private boolean followMe = true;
    private MenuItem menutItemFollowMe = null;
    //private int selectedRoadBlock;

    private TextView navHeaderTitle = null;
    private TextView nav_user = null;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String email = user.getEmail();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRefRoadblocks = database.getReference("rb");
    private String mSelectedRoadBlockTypeKey = null;
    private int mRoadBlockID = 0;
    private String mRoadBlockName = null;
    private Marker mCurMarker;
    private int mSelectedRoadBlockIdx = -1;
    private boolean mChoseDest = false;
    private Marker destMarker = null;
    private int RB_INDEX_INDEX_INSECURTY = 0;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private String mInsecAudioFileName = null;
    private String mIsInsecMarker = "false";
    private RoadBlocks.RoadBlock mRoadBlockToAdd = null;
    //private String uploadInsecAudioFileName = null;
    private String mRbKey = null;
    private String mNewRbType = "new_rb_type";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mInsecAudioFileName = getExternalCacheDir().getAbsolutePath();
        mInsecAudioFileName += "/audiorecordtest.3gp";

        //mRefRoadblocks.setValue("test");

        //selectedRoadBlock = DEFAULT_ROADBLOCK_ID;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Balabala");


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, Utils.GR_GS(MapsActivity.this, R.string.str_is_there_road_block), Snackbar.LENGTH_LONG)
                        .setAction(Utils.GR_GS(MapsActivity.this, R.string.menu_signal_road_block), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showAlertChoseRoadBlockType();

                                //Log.e(TAG, "onClick: Signaling" );
                            }
                        }).show();
            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        View hView =  navigationView.getHeaderView(0);
        nav_user = (TextView)hView.findViewById(R.id.navHeaderTitle);
        nav_user.setText(email);


        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                //Log.e(TAG, "*" + menuItem.getTitle() + "*" );

                if(menuItem.getItemId() == R.id.nav_choose_dest){
                    /*Log.e(TAG, "onNavigationItemSelected: Direction"  );

                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            RHYF, MAP_DEFAULT_BLOCK_ZOOM_LEVEL);
                    mMap.animateCamera(location);*/

                    //Log.e(TAG, "onNavigationItemSelected: -> chosing desitnation activated " );
                    mChoseDest = true;
                    followMe = false;

                    Toast.makeText(MapsActivity.this, "Veuillez clicker sur la carte pour choisir sa destination", Toast.LENGTH_LONG).show();

                }


                if(menuItem.getItemId() == R.id.nav_signal_road_block){
                    showAlertChoseRoadBlockType();
                }


                if(menuItem.getItemId() == R.id.nav_share_location){
                    Log.e(TAG, "onNavigationItemSelected: Share loc" );
                }

                if(menuItem.getItemId() == R.id.nav_follow_me){

                    menutItemFollowMe = menuItem;

                    menuItem.setChecked(!menuItem.isChecked());
                    String locked = menuItem.isChecked() ? Utils.GR_GS(MapsActivity.this, R.string.str_follow_me) : Utils.GR_GS(MapsActivity.this, R.string.str_stop_following_me);
                    menuItem.setTitle(locked);

                    followMe = menuItem.isChecked();

                    String followMeMsg = Utils.GR_GS(MapsActivity.this, R.string.str_msg_stop_following_me);

                    if(followMe) followMeMsg = Utils.GR_GS(MapsActivity.this, R.string.str_msg_start_following_me);


                    Snackbar.make(mapFragment.getView(), followMeMsg, Snackbar.LENGTH_LONG).show();

                    Log.e(TAG, "onNavigationItemSelected: -> " + menuItem.getTitle() + ", following me : " + followMe );
                }

                if(menuItem.getItemId() == R.id.nav_logout){
                    FirebaseAuth.getInstance().signOut();
                    Intent I = new Intent(MapsActivity.this, ActivityLogin.class);
                    startActivity(I);
                }

                drawer.closeDrawers();

                return false;
            }
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_choose_dest, R.id.nav_signal_road_block, R.id.nav_share_location, R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();
        //NavController navController = Navigation.findNavController(this, R.id.map);
        //NavController navController = Navigation.findNavController(this, R.id.map);
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.



        gps = new GPSTracker(MapsActivity.this);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                //execute every time, else your else part will work
                Log.e(TAG, "onCreate: loc perms granted" );
            }else{
                Log.e(TAG, "onCreate: loc perms not granted" );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }





        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, MS_DURATION_REFRESH_CURRENT_LOCATION);

        





    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }



    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {

            if(mChoseDest){ //Will chose destination
                Log.e(TAG, "onMapClick: -> dest : " + latLng.toString() );
                mChoseDest = false;

                mapCamGoto(latLng, FOLLOW_ME_ZOOM_LEVEL, true);


            }else{
                Log.e(TAG, "onMapClick: -> first activate dest" );
            }

        }
    };


    private ChildEventListener mChildEventListenerNewRoadBlock = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            Log.e(TAG, "onChildAdded: -> "  + dataSnapshot.toString());

            RoadBlocks.RoadBlock rb = dataSnapshot.getValue(RoadBlocks.RoadBlock.class);
            rb.setKey(dataSnapshot.getKey());
            Toast.makeText(MapsActivity.this, "Added new rb : " + rb.toString() + ", \nBy : " + rb.getSenderEmail() , Toast.LENGTH_LONG).show();

            vibrate();
            //// TODO: 2020-02-13 REMAKE DATABASE RB IDS
            int rbIcon = R.drawable.rb_insec;RoadBlocks.GetDummyRoadBlockIconFromFirebaseDBRBType(MapsActivity.this, rb.getRoadBlockIdx());
            Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(rbIcon)).position(rb.getLatLng()));
            //marker.setTag("DA TEST TAG");

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



            //email_name_lat_long_ts_[insec(true_false)]

            marker.setTag(dataSnapshot.getKey());//user.getEmail() + "_" + user.getDisplayName() + "_" + rb.getLat() + "_" + rb.getLon() + "_" + System.currentTimeMillis() + "_" + mIsInsecMarker);


        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            Log.e(TAG, "onChildRemoved: -> " + dataSnapshot.toString() );
            mMap.clear();
            vibrate();

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        //RoadBlocks.RoadBlock newRoadBlock = new RoadBlocks.RoadBlock(mRoadBlockName,  gps.getLatitude(), gps.getLongitude(), String.valueOf(System.currentTimeMillis()), mSelectedRoadBlockTypeKey);
       // String keyRb = mRefRoadblocks.push().getKey();
        //mRefRoadblocks.child(keyRb).setValue(newRoadBlock);


        mRefRoadblocks.addChildEventListener(mChildEventListenerNewRoadBlock);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRefRoadblocks.removeEventListener(mChildEventListenerNewRoadBlock);


    }

    private void signalRoadBlock() {



        //Log.e(TAG, "signalRoadBlock: \uD83D\uDE21" ); //😡


        mRbKey = mRefRoadblocks.push().getKey();
//mRefRoadblocks.child(mRbKey).setValue("test"); //😱😹😹😹



        final RoadBlocks.RoadBlock newRoadBlock = new RoadBlocks.RoadBlock(mRoadBlockName,  gps.getLatitude(), gps.getLongitude(), String.valueOf(System.currentTimeMillis()),  mNewRbType, FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //Log.e(TAG, "signalRoadBlock: WHAAT" );
        mRoadBlockToAdd = newRoadBlock;

        if(mSelectedRoadBlockIdx != RB_INDEX_INDEX_INSECURTY) {
            mRefRoadblocks.child(mRbKey).setValue(newRoadBlock);
            mIsInsecMarker = "false";
        }else{ // Insecurity option

            //Log.e(TAG, "signalRoadBlock: hum" );
            mIsInsecMarker = "true";




            AlertDialog alertDialogCreateNewBlocRoadType = new AlertDialog.Builder(MapsActivity.this)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Log.e(TAG, "signalRoadBlock: OKA"  );

                            //mRefRoadblocks.child(mRbKey).setValue(mRoadBlockToAdd);
                            recordInsecAudio();
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .create();


            alertDialogCreateNewBlocRoadType.setTitle("Signal d'inscecurite?");
            alertDialogCreateNewBlocRoadType.setMessage("Are you sure to make an insecurity awarness????");
            alertDialogCreateNewBlocRoadType.show();




        }







    }

    private void recordInsecAudio() {
        Log.e(TAG, "recordInsecAudio: -> will redocod and send audio to server" );

        startRecording();

        launchRecordingLatch(INSECURITY_AUDIO_RECORD_TIME_SEC);



    }

    private void startRecording() {


        //String str = user.getEmail() + "_" + user.getDisplayName() + "_" + mRoadBlockToAdd.getLat() + "_" + mRoadBlockToAdd.getLon() + "_" + System.currentTimeMillis() + "_" + mIsInsecMarker;

        //Log.e(TAG, "startRecording: \uD83D\uDE21 : fname -> " + str );

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(mInsecAudioFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void launchRecordingLatch(int timeSec) {

        new CountDownTimer(timeSec * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.e(TAG, "onTick: -> Recording ... already : " + millisUntilFinished / 1000 + " sec(s)." );
                //deleted
                //uploadInsecAudioFileName = user.getEmail() + "_"  + mRoadBlockToAdd.getLat() + "_" + mRoadBlockToAdd.getLon() + "_" + System.currentTimeMillis() + "_" + mIsInsecMarker;

            }

            public void onFinish() {
                stopRecording();

                // TODO: 2020-02-06 play audio for debug purposes
                //to delete later
                //playRecordedInsecurityAudioDBG();
                addInsecTag();
                uploadInsecurityAudioFile();
                mRefRoadblocks.child(mRbKey).setValue(mRoadBlockToAdd);
            }
        }.start();
    }

    private void addInsecTag() {
        Log.e(TAG, "addInsecTag: " );
    }

    //FirebaseStorage storage = FirebaseStorage.getInstance();
    //StorageReference storageRef = storage.getReference();

    // Create a reference to "mountains.jpg"
    StorageReference insecAudioFileRef = storageRef.child(getCurrentInsecAudioFileName() );

    private String getCurrentInsecAudioFileName() {

        // TODO: 2020-02-08 TO CONTINUE 
        return FirebaseAuth.getInstance().getCurrentUser().getEmail() + "_" + System.currentTimeMillis();
        //return user.getEmail() + "_" + user.getDisplayName() + "_" + mRoadBlockToAdd.getLat() + "_" + mRoadBlockToAdd.getLon() + "_" + System.currentTimeMillis() + "_" + mIsInsecMarker;
    }

    private void uploadInsecurityAudioFile() {


        insecAudioFileRef = storageRef.child("insec_audio/" + mRbKey);
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(mInsecAudioFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = insecAudioFileRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e(TAG, "onFailure: failure upload " );
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.e(TAG, "onSuccess: success upload -> " + taskSnapshot.toString() );
            }
        });

    }

    private void playRecordedInsecurityAudioDBG() {
        player = new MediaPlayer();
        try {
            player.setDataSource(mInsecAudioFileName);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.release();
                    player = null;
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }


    private void showAlertChoseRoadBlockType() {

        final View  viewDialogAddRoadBlock = getLayoutInflater().inflate(R.layout.dialog_roadblock_choice, null);

        final Spinner spinnerRbType = viewDialogAddRoadBlock.findViewById(R.id.spinnerRoblockTypes);

        spinnerRbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedRoadBlockIdx = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedRoadBlockIdx = -1;
            }
        });

        AlertDialog alertDialogCreateNewBlocRoadType = new AlertDialog.Builder(MapsActivity.this)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: -> " + mSelectedRoadBlockIdx);
                        signalRoadBlock();
                        //Log.e(TAG, "FAKYALL" );
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: -> " );

                    }
                })
                .create();


        alertDialogCreateNewBlocRoadType.setTitle("Choix de type de blockage");
        alertDialogCreateNewBlocRoadType.setView(viewDialogAddRoadBlock);
        alertDialogCreateNewBlocRoadType.show();

    }

    private void showAlertAddNewRoadBlockType() {
        final View  viewDialogAddRoadBlock = getLayoutInflater().inflate(R.layout.dialog_roadblock_add_type, null);
        AlertDialog alertDialogCreateNewBlocRoadType = new AlertDialog.Builder(MapsActivity.this)
                .setPositiveButton("Add New Cat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: " );
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();


        alertDialogCreateNewBlocRoadType.setTitle("Ajouter Nouveau type de bouchon");
        alertDialogCreateNewBlocRoadType.setView(viewDialogAddRoadBlock);
        alertDialogCreateNewBlocRoadType.show();
    }

    private void toggleFollowMe(boolean followMe) {



        this.followMe = followMe;

        if(menutItemFollowMe != null){


            String locked = menutItemFollowMe.isChecked() ? Utils.GR_GS(MapsActivity.this, R.string.str_follow_me) : Utils.GR_GS(MapsActivity.this, R.string.str_stop_following_me);
            menutItemFollowMe.setTitle(locked);


        }

    }

    private void TimerMethod()
    {
        // TODO: 2020-01-23 SHOW CUR LOC TICK
        this.runOnUiThread(getCurrentLocationNewData);
    }

    private Runnable getCurrentLocationNewData = new Runnable() {
        public void run() {

            showCurrentLocation();

        }
    };

    private void showCurrentLocation() {



        if(mMap != null && curLocationMarker != null) {

            curLocationMarker.remove();
            curLocationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_me))
                    .position(gps.getLatLng())
                    .title("My Location"));


            if(followMe) {



                // TODO: 2020-01-29 ADD Settings IN OPPO ( follow me zoom level and zoom on follow

                if(firstShot) {

                    /*CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            gps.getLatLng(), FOLLOW_ME_ZOOM_LEVEL);
                    mMap.animateCamera(location);*/
                    mapCamGoto(gps.getLatLng(), FOLLOW_ME_ZOOM_LEVEL, false);

                firstShot = false;
                }else {
                    CameraUpdate location = CameraUpdateFactory.newLatLng(
                            gps.getLatLng());
                    mMap.animateCamera(location);
                }

                Log.e(TAG, "showCurrentLocation: bearing : " + gps.getBearing() );

                //curLocationMarker.showInfoWindow();

                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                        //+ gps.latitude + "\nLong: " + gps.longitude, Toast.LENGTH_SHORT).show();
            }
        }

        //Log.e(TAG, "showCurrentLocation: " );
    }

    private void mapCamGoto(LatLng latLng, float zoomLevel, boolean addMarker) {


        if(addMarker){
            if( destMarker != null) destMarker.remove();
            destMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ma destination"));
        }

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            latLng, zoomLevel);
                    mMap.animateCamera(location);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        Log.e(TAG, "onOptionsItemSelected: -> " + item.getTitle() );

        if(item.getItemId() == R.id.action_settings){

            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(intent);

        }

        if(item.getItemId() == R.id.action_debug_clear){


            mRefRoadblocks.setValue(null);
            storageRef.child("insec_audio/").delete();

            if(mMap != null){
                mMap.clear();
            }


        }

        return super.onOptionsItemSelected(item);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {



        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {



                Intent intent = new Intent(MapsActivity.this, ActivityMarkerDetails.class);
                String markerTitle = String.valueOf(System.currentTimeMillis()); // new Date().toString();

                if(marker.getTag() != null) {
                    intent.putExtra("tag", marker.getTag().toString());

                    // TODO: 2020-02-08 should check new activity jump logic

                    startActivity(intent);
                }


                return false;
            }
        });

        curLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(gps.getLatLng())
                .title("My Location"));


        // TODO: 2020-01-24 FOR MAP DESIGN 
        /*try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }*/

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {



                Log.e(TAG, "onMyLocationButtonClick: -> " + gps.getLatitude() + ", " + gps.getLongitude() );


                curLocationMarker.remove();
                curLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(gps.getLatLng())
                        .title("My Location"));

                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        gps.getLatLng(), MAP_DEFAULT_BLOCK_ZOOM_LEVEL);
                mMap.animateCamera(location);

                curLocationMarker.showInfoWindow();


                return false;
            }
        });



    }

    @Override
    protected void onPause() {
        super.onPause();

        followMe = false;
        Log.e(TAG, "onPause: " );
    }

    @Override
    protected void onResume() {
        super.onResume();
        followMe = true;
        Log.e(TAG, "onResume: " );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);




        return true;
    }


    public void replayInsecAudio(View view) {
        Log.e(TAG, "replayInsecAudio: " );
    }

}
