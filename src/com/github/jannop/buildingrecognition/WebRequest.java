package com.github.jannop.buildingrecognition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebRequest {

    private final URL url;

    public WebRequest(String urlString) {
        url = getUrlFromString(urlString);
    }

    public JSONObject get() {
        return getJSONObject(getResponseMessage(url, null));
    }

    public JSONObject post(String content) {
        return getJSONObject(getResponseMessage(url, content));
    }

    private static URL getUrlFromString(String address) {
        try {
            return new URL(address);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static String getResponseMessage(URL url, String content) {
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);

            byte[] contentBytes = content != null ? content.getBytes() : null;

            if (contentBytes != null) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", "" + contentBytes.length);
            }

            connection.connect();

            if (contentBytes != null) {
                OutputStream stream = connection.getOutputStream();
                stream.write(contentBytes);
                stream.flush();
                stream.close();
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            reader.close();
            connection.disconnect();

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private JSONObject getJSONObject(String message) {
        if (message != null) {
            try {
                return new JSONObject(message);
            } catch (JSONException e) {
            }
        }
        return null;
    }
}
