package com.example.balabala_beta.dummy;

import android.view.MenuItem;

import com.example.balabala_beta.R;
import com.example.balabala_beta.Utils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RoadBlocks {

    public static final int NUM_ROAD_BLOCKS = 3;
    public static RoadBlock getRoadBlock(int roadBlockID) {

        //BitmapDescriptorFactory.fromResource(roadBlockID)
        RoadBlock roadBlockCarBikeAcc = new RoadBlock("Accident Voiture-Moto", new LatLng(0,0), "timestamp");
        RoadBlock roadBlockSchool = new RoadBlock("Ecole", new LatLng(0,0), "timestamp");
        RoadBlock roadBlockCarJam = new RoadBlock("Car jam", new LatLng(0,0), "timestamp");

        ArrayList<RoadBlock> roadBlocks = new ArrayList<>();

        roadBlocks.add(roadBlockCarBikeAcc);
        roadBlocks.add(roadBlockSchool);
        roadBlocks.add(roadBlockCarJam);




        return roadBlocks.get(roadBlockID);
    }

    public static int getDummyRoadBlockIcon(int index){
        int roadBlocksIconsIDs[] = new int[]{R.drawable.rb_car_bike_acc,  R.drawable.rb_school, R.drawable.rd_blc_car_jam};
        return roadBlocksIconsIDs[index];
    }

    public static class RoadBlock {

        private final LatLng latLng;
        private final String timestamp;
        private final String title;

        public RoadBlock(String title, LatLng latLng, String timestamp) {
            this.title = title;
            this.latLng = latLng;
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
