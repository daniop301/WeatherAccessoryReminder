package ca.group3.weatheraccessoryreminder.Widgets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import ca.group3.weatheraccessoryreminder.MainActivity;

/**
 * Created by stefa on 12/5/2017.
 */

public class LocationWidget {
    private LocationManager locationManager;
    private MainActivity mainActivity;

    public LocationWidget(LocationManager locationManager, MainActivity mainActivity) {
        this.locationManager = locationManager;
        this.mainActivity = mainActivity;
    }

    public Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long GPSLocationTime = 0;
            if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

            long NetLocationTime = 0;

            if (null != locationNet) {
                NetLocationTime = locationNet.getTime();
            }

            if ( 0 < GPSLocationTime - NetLocationTime ) {
                return locationGPS;
            }
            else {
                return locationNet;
            }
        }

        return null; //permissions not granted
    }
}
