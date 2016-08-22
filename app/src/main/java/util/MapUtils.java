package util;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class MapUtils {

    public static double distBetween(LatLng pos1, LatLng pos2) {
        return distance(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude);
    }
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 100.0;
        dist *= 10.0;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.valueOf(df.format(dist).trim());
    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
