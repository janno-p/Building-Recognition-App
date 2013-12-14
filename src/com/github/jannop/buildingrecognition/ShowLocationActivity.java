package com.github.jannop.buildingrecognition;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ShowLocationActivity extends Activity {
    private final GeoPoint testLocation = new GeoPoint(59.402474, 24.69505);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        MapView mapView = (MapView)findViewById(R.id.mapView);
        mapView.getController().setZoom(18);
        mapView.getController().animateTo(testLocation);
        mapView.setMultiTouchControls(true);
        new GetAddressTask().execute(testLocation);
    }

    public void onNo(View view) {
        Intent intent = new Intent(getApplicationContext(), SelectLocationActivity.class);
        startActivity(intent);
    }

    private class GetAddressTask extends AsyncTask<GeoPoint, Void, String> {
        @Override
        protected String doInBackground(GeoPoint... params) {
            URL url = getUrlFromString(params[0]);
            String jsonContent = getInputStream(url);
            return decodeString(jsonContent);
        }

        @Override
        protected void onPostExecute(String result) {
            TextView textView = (TextView)findViewById(R.id.textView);
            if (result != null && !result.isEmpty()) {
                textView.setText(result);
            } else {
                textView.setText("Bla!");
            }
        }

        private String getAddress(GeoPoint point) {
            return "http://nominatim.openstreetmap.org/reverse?format=xml&lat="
                    + point.getLatitude()
                    + "&lon="
                    + point.getLongitude()
                    + "&zoom=18&addressdetails=1&format=json";
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

        private String decodeString(String jsonContent) {
            if (jsonContent == null) {
                return null;
            }

            try {
                JSONObject o = new JSONObject(jsonContent);
                return o.getString("display_name");
            } catch (JSONException e) {
                return null;
            }
        }
    }
}
