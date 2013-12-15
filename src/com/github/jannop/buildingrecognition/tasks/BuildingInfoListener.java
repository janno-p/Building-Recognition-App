package com.github.jannop.buildingrecognition.tasks;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public interface BuildingInfoListener {
    void updateAddress(String address);

    void updateMap(GeoPoint location, ArrayList<GeoPoint> building);
}
