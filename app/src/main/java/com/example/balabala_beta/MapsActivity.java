package com.example.balabala_beta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;


import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //private static final long LOCATION_REFRESH_TIME = 5000;
    private static final float LOCATION_REFRESH_DISTANCE = 5;
    private static final long MS_DURATION_REFRESH_CURRENT_LOCATION = 5000;
    private static final float MAP_DEFAULT_BLOCK_ZOOM_LEVEL = 20f;
    private static final int DEFAULT_ROADBLOCK_ID = 3;
    private static final int NUM_DEF_ROAD_BLOCKS = 3;
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
    private int selectedRoadBlock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        selectedRoadBlock = DEFAULT_ROADBLOCK_ID;

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
                                signalRoadBlock();
                            }
                        }).show();
            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                //Log.e(TAG, "*" + menuItem.getTitle() + "*" );

                if(menuItem.getItemId() == R.id.nav_choose_dest){
                    Log.e(TAG, "onNavigationItemSelected: Direction"  );

                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            RHYF, MAP_DEFAULT_BLOCK_ZOOM_LEVEL);
                    mMap.animateCamera(location);

                }


                if(menuItem.getItemId() == R.id.nav_signal_road_block){
                    signalRoadBlock();
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

    private void signalRoadBlock() {


        // TODO: 2020-01-24 to be continued ( only for testing purposes ) 
        Log.e(TAG, "signalRoadBlock: " );


        final View dialogViewChoseRoadBlock = getLayoutInflater().inflate(R.layout.dialog_roadblock_choice, null);
        final View  viewDialogAddRoadBlock = getLayoutInflater().inflate(R.layout.dialog_roadblock_add_type, null);


        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {




                        //Log.e(TAG, "onClick: envoyer" );

                        //to continue

                        //Random Dice = new Random();
                        int roadBlockID = selectedRoadBlock;//which; //Dice.nextInt(RoadBlocks.NUM_ROAD_BLOCKS);

                        if(roadBlockID < NUM_DEF_ROAD_BLOCKS) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(gps.getLatLng())
                                    .title(RoadBlocks.getRoadBlock(roadBlockID).getTitle())
                                    .icon(BitmapDescriptorFactory.fromResource(RoadBlocks.getDummyRoadBlockIcon(roadBlockID)))
                                    .rotation(0)
                                    .draggable(false)
                            ;

                            mMap.addMarker(markerOptions);
                        }else{
                            Log.e(TAG, "onClick: -> Implement dialog for adding new roadblock type" );











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

                        Log.e(TAG, "onClick: -> roadBlockID : " + roadBlockID );


                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: annuler" );

                    }
                })
                .create();
        alertDialog.setTitle("Choisir type de bloquage");
        alertDialog.setView(dialogViewChoseRoadBlock);



        alertDialog.show();








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

            if(followMe) {
                curLocationMarker.remove();
                curLocationMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_me))
                        .position(gps.getLatLng())
                        .title("My Location"));

                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        gps.getLatLng(), 20);
                mMap.animateCamera(location);

                //curLocationMarker.showInfoWindow();

                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                        + gps.latitude + "\nLong: " + gps.longitude, Toast.LENGTH_SHORT).show();
            }
        }

        //Log.e(TAG, "showCurrentLocation: " );
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        Log.e(TAG, "onOptionsItemSelected: -> " + item.getTitle() );

        if(item.getItemId() == R.id.action_settings){

            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(intent);

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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Log.e(TAG, "onMarkerClick: -> " + marker.toString() );



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


    public void onRoadBlockSelected(View view) {
        selectedRoadBlock = Integer.parseInt(view.getTag().toString());
    }
}
