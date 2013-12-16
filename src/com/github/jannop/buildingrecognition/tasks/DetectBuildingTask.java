package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.activities.ShowLocationActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DetectBuildingTask extends AsyncTask<GeoPoint, Void, BuildingDetails> {
    private final ShowLocationActivity activity;

    public DetectBuildingTask(ShowLocationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected BuildingDetails doInBackground(GeoPoint... params) {
        GeoPoint location = params[0];

        String address = getAddress(location);
        URL url = getUrlFromString(address);

        JSONObject details = location != null ? decodeObject(getInputStream(url)) : null;

        return parseBuildingDetails(details);
    }

    @Override
    protected void onPostExecute(BuildingDetails building) {
        activity.completeBuildingDetection(building);
    }

    private static BuildingDetails parseBuildingDetails(JSONObject details) {
        if (details == null) {
            return null;
        }

        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

        if (details != null) {
            JSONArray array = getArrayProperty(details, "building_nodes");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    try {
                        JSONArray vector = array.getJSONArray(i);
                        points.add(new GeoPoint(vector.getDouble(0), vector.getDouble(1)));
                    } catch (JSONException e) {
                    }
                }
            }
        }

        if (points.size() < 1) {
            return null;
        }

        BuildingDetails buildingDetails = new BuildingDetails();
        buildingDetails.address = getAddressString(details);
        buildingDetails.polygon = points;

        return buildingDetails;
    }

    private static String getAddress(GeoPoint location) {
        return "http://bldrecog.appspot.com/check_location?"
                + "lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude();
    }

    private static URL getUrlFromString(String address) {
        try {
            return new URL(address);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static String getInputStream(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static JSONObject decodeObject(String jsonContent) {
        if (jsonContent != null) {
            try {
                return new JSONObject(jsonContent);
            } catch (JSONException e) {
            }
        }
        return null;
    }

    private static String getAddressString(JSONObject building) {
        if (building == null)
            return null;
        StringBuilder sb = new StringBuilder();
        String streetName = getStringProperty(building, "addr:street");
        if (streetName != null)
            sb.append(streetName);
        String houseNumber = getStringProperty(building, "addr:housenumber");
        if (houseNumber != null)
            sb.append((sb.length() > 0 ? " - " : "") + houseNumber);
        String cityName = getStringProperty(building, "addr:city");
        if (cityName != null)
            sb.append((sb.length() > 0 ? ", " : "") + cityName);
        String countryCode = getStringProperty(building, "addr:country");
        if (countryCode != null)
            sb.append((sb.length() > 0 ? " " : "") + "(" + countryCode + ")");
        return sb.toString();
    }

    private static String getStringProperty(JSONObject building, String propertyName) {
        try {
            if (building.has(propertyName))
                return building.getString(propertyName);
        } catch (JSONException e) {
        }
        return null;
    }

    private static JSONArray getArrayProperty(JSONObject building, String propertyName) {
        try {
            if (building.has(propertyName))
                return building.getJSONArray(propertyName);
        } catch (JSONException e) {
        }
        return null;
    }
}
