package com.example.balabala_beta;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "balabala";

    private static final LatLng RHYF = new LatLng(-11.629749, 27.488710);
    private static final LatLng P1 = new LatLng(-11.625700, 27.485300);
    private static final LatLng P2 = new LatLng(-11.620, 27.480);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        Circle circle1, circle2, circle3 = null;


        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                Log.e(TAG, "onCircleClick: -> id : " + circle.getId());


                String msg1 = "Blv. M'Siri Bloque pour l'instant!";
                String msg2 = "Av Changalele Bloque pour l'instant!";
                String msg3 = "Rte. KASAPA Bloque pour l'instant!";


                String curMsg = msg1;

                if(circle.getId().equals("ci0")){
                    curMsg = msg1;
                }

                if(circle.getId().equals("ci1")){
                    curMsg = msg2;
                }

                if(circle.getId().equals("ci2")){
                    curMsg = msg3;
                }



                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                alertDialog.setTitle("Bouchon!");

                alertDialog.setMessage(curMsg);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addMarker(new MarkerOptions().position(RHYF).title("Docta Rhyf's Pos").visible(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addMarker(new MarkerOptions().position(P1).title("Bouchon Changalele").visible(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addMarker(new MarkerOptions().position(P2).title("Bouchon KASAPA").visible(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        circle1 = mMap.addCircle(new CircleOptions()
                .center(RHYF)
                .radius(200)
                .strokeColor(Color.RED)
                .fillColor(Color.YELLOW));

        circle1.setClickable(true);

        circle2 = mMap.addCircle(new CircleOptions()
                .center(P1)
                .radius(200)
                .strokeColor(Color.GREEN)
                .fillColor(Color.BLUE));

        circle2.setClickable(true);

        circle3 = mMap.addCircle(new CircleOptions()
                .center(P2)
                .radius(300)
                .strokeColor(Color.BLACK)
                .fillColor(Color.WHITE));

        circle3.setClickable(true);




        final LatLng coordinate = P2; //Store these lat lng values somewhere. These should be constant.


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        coordinate, 15);
                mMap.animateCamera(location);
            }
        }, 4000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
