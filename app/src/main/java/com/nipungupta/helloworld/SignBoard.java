package com.nipungupta.helloworld;

/**
 * Created by Nipun Gupta on 4/23/2016.
 */
public class SignBoard {

    private double latitude;
    private double longitude;
    private String data;

    public SignBoard(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
        this.data = "";
    }

    public SignBoard(double lat, double lon, String data) {
        this.latitude = lat;
        this.longitude = lon;
        this.data = data;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static double distanceGPS(double lat1, double lon1, double lat2, double lon2) {
        double d2r = Math.PI/180;
        double dlon = (lon2-lon1)*d2r;
        double dlat = (lat2-lat1)*d2r;
        double a = Math.pow(Math.sin(dlat/2.0),2) + Math.cos(lat1*d2r)*Math.cos(lat2*d2r)*Math.pow(Math.sin(dlon/2.0),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = 6367*c*1000;

        return dist;
    }

    public static double isPointBetweenSignboards(double lat, double lon, SignBoard sign1, SignBoard sign2) {
        double distSign1 = Math.abs(distanceGPS(lat, lon, sign1.getLatitude(), sign1.getLongitude()));
        double distSign2 = Math.abs(distanceGPS(lat, lon, sign2.getLatitude(), sign2.getLongitude()));
        double sign1sign2 = Math.abs(distanceGPS(sign2.getLatitude(), sign2.getLongitude(), sign1.getLatitude(), sign1.getLongitude()));
        double collinearity = Math.abs(distSign1 + distSign2 - sign1sign2);
//        if(collinearity<5) {
//            return true;
//        }
//        else {
//            return false;
//        }
        return collinearity;
    }

    public static int getNextSignBoard(double lat, double lon, final SignBoard[] signBoards) {
        double[] collCheck = new double[signBoards.length-1];
        double minimum = Double.MAX_VALUE;
        int minIdx = -1;
        for(int i=0; i<signBoards.length-1; i++) {
            collCheck[i] = isPointBetweenSignboards(lat, lon, signBoards[i], signBoards[i+1]);
            if(collCheck[i] < minimum) {
                minimum = collCheck[i];
                minIdx = i+1;
            }
        }
        return minIdx;
    }
}
