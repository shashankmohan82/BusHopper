package com.example.hopper.bushopper;



        import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sHIVAM on 4/9/2016.
 */
public class BusLocation {
    double latitude = 25.7965225;
    double longitude = 85.1883757;
    public LatLng busLocation(int RouteID){

        LatLng latLng = new LatLng(latitude,longitude);
        return latLng;
    }

    public String busStatus(int RouteID){
        String status = "4:50 pm";
        return status;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
