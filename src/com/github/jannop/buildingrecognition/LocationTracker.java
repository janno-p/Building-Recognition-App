package com.github.jannop.buildingrecognition;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import org.osmdroid.util.GeoPoint;

public class LocationTracker {
    private final Context context;
    private final LocationManager locationManager;

    public LocationTracker(Context context) {
        this.context = context;
        this.locationManager = (LocationManager)context.getSystemService(Service.LOCATION_SERVICE);
    }

    public GeoPoint getLocation() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return new GeoPoint(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return new GeoPoint(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isLocationAcquirable() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}
