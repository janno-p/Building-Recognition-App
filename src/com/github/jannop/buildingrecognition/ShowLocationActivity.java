package com.github.jannop.buildingrecognition;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ShowLocationActivity extends Activity {
    //private final GeoPoint testLocation = new GeoPoint(59.402474, 24.69505);
    //private final GeoPoint testLocation = new GeoPoint(59.40298, 24.69381);
    private final GeoPoint testLocation = new GeoPoint(59.40364, 24.69097);
    private TextView textView;
    private MapView mapView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        textView = (TextView)findViewById(R.id.textView);
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.getController().setZoom(15);
        mapView.getController().animateTo(testLocation);
        mapView.setMultiTouchControls(true);
        new DetectBuildingTask().execute(testLocation);
    }

    public void onNo(View view) {
        Intent intent = new Intent(getApplicationContext(), SelectLocationActivity.class);
        startActivity(intent);
    }

    public void setLocation(String locationString) {
        textView.setText("You are at\n" + locationString);
    }

    public void setOverlay(ArrayList<GeoPoint> points) {
        if (points == null || points.size() < 1)
            return;

        Polygon path = new Polygon(this);
        path.setFillColor(Color.argb(164, 0, 255, 0));
        path.setStrokeColor(Color.argb(255, 0, 128, 0));
        path.setStrokeWidth(2);
        for (GeoPoint point : points) {
            path.addPoint(point.getLatitudeE6(), point.getLongitudeE6());
        }
        path.addPoint(points.get(0).getLatitudeE6(), points.get(0).getLongitudeE6());

        mapView.getOverlays().clear();
        mapView.getOverlays().add(path);
    }

    private class DetectBuildingTask extends AsyncTask<GeoPoint, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(GeoPoint... params) {
            URL url = getUrlFromString(params[0]);
            String jsonContent = getInputStream(url);
            return decodeObject(jsonContent);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result == null)
                return;

            setLocation(getAddressString(result));

            ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
            JSONArray array = getArrayProperty(result, "building_nodes");
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONArray vector = array.getJSONArray(i);
                    points.add(new GeoPoint(vector.getDouble(0), vector.getDouble(1)));
                } catch (JSONException e) {
                    continue;
                }
            }

            setOverlay(points);
        }

        private String getAddress(GeoPoint point) {
            return "http://bldrecog.appspot.com/check_location?"
                    + "lat=" + point.getLatitude()
                    + "&lon=" + point.getLongitude();
        }

        private URL getUrlFromString(GeoPoint point) {
            try {
                return new URL(getAddress(point));
            } catch (MalformedURLException e) {
                return null;
            }
        }

        private String getInputStream(URL url) {
            try {
                URLConnection connection = url.openConnection();
                connection.connect();
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
            if (jsonContent != null) {
                try {
                    return new JSONObject(jsonContent);
                } catch (JSONException e) {
                }
            }
            return null;
        }

        private String getAddressString(JSONObject building) {
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
            } catch (JSONException e) { }
            return null;
        }

        private JSONArray getArrayProperty(JSONObject building, String propertyName) {
            try {
                if (building.has(propertyName))
                    return building.getJSONArray(propertyName);
            } catch (JSONException e) { }
            return null;
        }
    }
}
