package com.example.balabala_beta.dummy;

import androidx.annotation.NonNull;

import com.example.balabala_beta.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RoadBlocks {

    public static final int NUM_ROAD_BLOCKS = 3;
    public static  RoadBlock getRoadBlock(int roadBlockID) {

        //BitmapDescriptorFactory.fromResource(roadBlockID)
        RoadBlock roadBlockCarBikeAcc = new RoadBlock("Accident Voiture-Moto", 0,0, "timestamp", getRoadBlockType(0));
         RoadBlock roadBlockSchool = new RoadBlock("Ecole", 0,0, "timestamp", getRoadBlockType(1));
         RoadBlock roadBlockCarJam = new RoadBlock("Embouteillage", 0,0, "timestamp", getRoadBlockType(2));
        //RoadBlock roadBlockCarJam = new RoadBlock("Accident", new LatLng(0,0), "timestamp");

        ArrayList< RoadBlock> roadBlocks = new ArrayList<>();

        roadBlocks.add(roadBlockCarBikeAcc);
        roadBlocks.add(roadBlockSchool);
        roadBlocks.add(roadBlockCarJam);




        return roadBlocks.get(roadBlockID);
    }

    public static String getRoadBlockType(int idx){

        String roadBlockType = "Autre";
        final List<String> ROADBLOKCS = new ArrayList<>();

        ROADBLOKCS.add(0, "rbType_accid");
        ROADBLOKCS.add(1, "rbType_ecole");
        ROADBLOKCS.add(2, "rbType_embout");

        if(idx > ROADBLOKCS.size() - 1){
            idx = ROADBLOKCS.size();

        }else{
            roadBlockType = ROADBLOKCS.get(idx);
        }

        return roadBlockType;


    }

    public static int getDummyRoadBlockIcon(int index){
        int roadBlocksIconsIDs[] = new int[]{R.drawable.rb_car_bike_acc,  R.drawable.rb_school, R.drawable.rd_blc_car_jam};
        return roadBlocksIconsIDs[index];
    }

    public static class RoadBlock  {


        private double lat;
        private double lon;
        private String timestamp;
        private String title;
        private String roadBlockType;

        public RoadBlock(){

        }

        public RoadBlock(String title, double lat, double lon, String timestamp, String roadBlockType) {
            this.title = title;
            this.lat = lat;
            this.lon = lon;
            this.timestamp = timestamp;
            this.roadBlockType = roadBlockType;
        }


        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRoadBlockType() {
            return roadBlockType;
        }

        public void setRoadBlockType(String roadBlockType) {
            this.roadBlockType = roadBlockType;
        }

        public LatLng getLatLng() {
            return new LatLng(lat, lon);
        }


        @Override
        public String toString() {
            return "Lat : " + this.lat + ", Long : " + this.lon;// + ", key : " + key;
        }
    }
}
