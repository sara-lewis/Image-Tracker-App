package us.ait.android.aitfinalproject.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationMonitor implements LocationListener{

    // will be used to communicate with MainActivity
    public interface LocationObserver {
        public void locationAvailable(Location location);
        public void locationInfo(String info);
    }

    private LocationObserver locationObserver = null;
    private LocationManager locationManager = null;

    public void startLocationMonitoring(LocationObserver locationObserver, LocationManager locationManager) throws SecurityException{
        this.locationObserver = locationObserver;
        this.locationManager = locationManager;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0,this);
    }

    public void stopLocationMonitoring(){
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        locationObserver.locationAvailable(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        locationObserver.locationInfo("Status: " + provider + ", " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationObserver.locationInfo("Enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationObserver.locationInfo("Disabled " + provider);
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}

