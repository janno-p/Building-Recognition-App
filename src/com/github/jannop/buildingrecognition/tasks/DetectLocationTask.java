package com.github.jannop.buildingrecognition.tasks;

import android.app.Service;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import com.github.jannop.buildingrecognition.activities.ShowLocationActivity;

public class DetectLocationTask extends AsyncTask<Void, Void, Location> implements LocationListener {
    private final ShowLocationActivity activity;

    private Location location;

    public DetectLocationTask(ShowLocationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Location doInBackground(Void... params) {
        LocationManager manager = (LocationManager)activity.getSystemService(Service.LOCATION_SERVICE);

        try {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.getMainLooper());
                waitForLocation();
                manager.removeUpdates(this);
                if (location != null)
                    return location;
            }
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
                waitForLocation();
                manager.removeUpdates(this);
                if (location != null)
                    return location;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean waitForLocation() {
        long timeToSleep = 60000;
        long step = 1000;
        while (timeToSleep > 0 && location == null) {
            try {
                Thread.sleep(step);
            } catch (InterruptedException e) {
            }
            timeToSleep -= step;
        }
        return location != null;
    }

    @Override
    protected void onPostExecute(Location result) {
        activity.setLocation(result);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
}
