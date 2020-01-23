package com.example.balabala_beta;

import android.Manifest;
import android.content.pm.PackageManager;


import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //private static final long LOCATION_REFRESH_TIME = 5000;
    private static final float LOCATION_REFRESH_DISTANCE = 5;
    private static final long MS_DURATION_REFRESH_CURRENT_LOCATION = 5000;
    private static final float MAP_DEFAULT_BLOCK_ZOOM_LEVEL = 20f;
    private GoogleMap mMap;
    //ArrayList markerPoints = new ArrayList();
    //Marker curPosMarker;

    private static final String TAG = "balabala";

    private static final LatLng RHYF = new LatLng(-11.629749, 27.488710);
    //private static final LatLng P1 = new LatLng(-11.625700, 27.485300);
    //private static final LatLng P2 = new LatLng(-11.620, 27.480);


    private AppBarConfiguration mAppBarConfiguration;
    private LocationManager mLocationManager;

    // GPSTracker class
    GPSTracker gps;
    private Timer myTimer;

    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private float defaultGraphicsScaleKM = 0.25f / 128f;
    private double defaultGraphicsRectScaleRectHeight = 6.0f;
    private Marker curLocationMarker;
    private boolean followMe = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Balabala");


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                //Log.e(TAG, "*" + menuItem.getTitle() + "*" );

                if(menuItem.getItemId() == R.id.nav_choose_dest){
                    Log.e(TAG, "onNavigationItemSelected: Direction"  );

                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            RHYF, 15);
                    mMap.animateCamera(location);

                }


                if(menuItem.getItemId() == R.id.nav_signal_road_block){
                    Log.e(TAG, "onNavigationItemSelected: -> SIgnal road block " );
                }


                if(menuItem.getItemId() == R.id.nav_share_location){
                    Log.e(TAG, "onNavigationItemSelected: Share loc" );
                }

                if(menuItem.getItemId() == R.id.nav_follow_me){

                    menuItem.setChecked(!menuItem.isChecked());
                    String locked = menuItem.isChecked() ? Utils.GR_GS(MapsActivity.this, R.string.str_follow_me) : Utils.GR_GS(MapsActivity.this, R.string.str_stop_following_me);
                    menuItem.setTitle(locked);

                    followMe = menuItem.isChecked();

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
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        gps = new GPSTracker(MapsActivity.this);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                //execute every time, else your else part will work
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
                        .position(gps.getLatLng())
                        .title("My Location"));

                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        gps.getLatLng(), 20);
                mMap.animateCamera(location);

                curLocationMarker.showInfoWindow();

                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                        + gps.latitude + "\nLong: " + gps.longitude, Toast.LENGTH_SHORT).show();
            }
        }

        Log.e(TAG, "showCurrentLocation: " );
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

        curLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(gps.getLatLng())
                .title("My Location"));



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

    /*private Polygon drawRoadBlockIntensityByLatLong(LatLng latLng, float scale, double rectScale){

        LatLng latLng1 = latLng;
        LatLng latLng2 = new LatLng(latLng.latitude, latLng.longitude + (scale / rectScale));
        LatLng latLng3 = new LatLng(latLng.latitude + scale, latLng.longitude + ( scale / rectScale));
        LatLng latLng4 = new LatLng(latLng.latitude + scale, latLng.longitude);
        LatLng latLng5 = latLng1;

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                //.add(new LatLng(0, 0), new LatLng(0, 5), new LatLng(5, 5), new LatLng(5, 0), new LatLng(0, 0))
                .add(latLng1, latLng2, latLng3, latLng4, latLng5)
                .strokeColor(Color.RED)
                .strokeWidth(5f)
                .geodesic(true)
                .fillColor(Color.BLUE));


        polygon.setClickable(true);

        return polygon;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);




        return true;
    }








}
