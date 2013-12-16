package com.github.jannop.buildingrecognition;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class BuildingDetails implements Serializable {
    public String address;

    public String name;

    public ArrayList<GeoPoint> polygon;
}
