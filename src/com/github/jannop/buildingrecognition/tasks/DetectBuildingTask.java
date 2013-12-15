package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import android.util.Log;
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

public class DetectBuildingTask extends AsyncTask<GeoPoint, Void, JSONObject> {
    private final BuildingInfoListener listener;

    private GeoPoint position;

    public DetectBuildingTask(BuildingInfoListener listener) {
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(GeoPoint... params) {
        position = params[0];
        return position != null
                ? decodeObject(getInputStream(getUrlFromString()))
                : null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        listener.updateAddress(getAddressString(result));

        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

        if (result != null) {
            JSONArray array = getArrayProperty(result, "building_nodes");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    try {
                        JSONArray vector = array.getJSONArray(i);
                        points.add(new GeoPoint(vector.getDouble(0), vector.getDouble(1)));
                    } catch (JSONException e) {
                        continue;
                    }
                }
            }
        }

        listener.updateMap(position, points);
    }

    private String getAddress() {
        return "http://bldrecog.appspot.com/check_location?"
                + "lat=" + position.getLatitude()
                + "&lon=" + position.getLongitude();
    }

    private URL getUrlFromString() {
        Log.d("##########GETURL", "#############GETURL");
        try {
            return new URL(getAddress());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private String getInputStream(URL url) {
        Log.d("##########GETINPUTSTR", "#############GETINPUTSTR");
        try {
            URLConnection connection = url.openConnection();
            //connection.connect();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private JSONObject decodeObject(String jsonContent) {
        Log.d("##########DECODE", "#############DECODE");
        if (jsonContent != null) {
            try {
                return new JSONObject(jsonContent);
            } catch (JSONException e) {
            }
        }
        return null;
    }

    private String getAddressString(JSONObject building) {
        Log.d("##########ADDSTR", "#############ADDSTR");
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

    private String getStringProperty(JSONObject building, String propertyName) {
        try {
            if (building.has(propertyName))
                return building.getString(propertyName);
        } catch (JSONException e) {
        }
        return null;
    }

    private JSONArray getArrayProperty(JSONObject building, String propertyName) {
        try {
            if (building.has(propertyName))
                return building.getJSONArray(propertyName);
        } catch (JSONException e) {
        }
        return null;
    }
}
