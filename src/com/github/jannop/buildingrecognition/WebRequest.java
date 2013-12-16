package com.github.jannop.buildingrecognition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebRequest {

    private final URL url;

    public WebRequest(String urlString) {
        url = getUrlFromString(urlString);
    }

    public JSONObject execute() {
        String message = getResponseMessage(url);

        if (message != null) {
            try {
                return new JSONObject(message);
            } catch (JSONException e) {
            }
        }

        return null;
    }

    private static URL getUrlFromString(String address) {
        try {
            return new URL(address);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static String getResponseMessage(URL url) {
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
}
