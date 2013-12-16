package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.activities.EditLocationActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveBuildingTagTask extends AsyncTask<Object, Void, Object[]> {
    private final EditLocationActivity activity;

    public SaveBuildingTagTask(EditLocationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        BuildingDetails building = (BuildingDetails)params[0];
        String username = (String)params[1];

        try {
            String parameters = "username=" + username + "&building=" + building.id + "&name=" + building.name;

            URL url = new URL("http://bldrecog.appspot.com/addtag");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length", "" + parameters.getBytes().length);
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(parameters);
            out.flush();
            out.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            urlConnection.disconnect();

            String message = sb.toString();
            if (message != null) {
                try {
                    JSONObject obj = new JSONObject(message);
                    boolean result = obj.getBoolean("success");
                    if (result) {
                        return new Object[] { true, "OK" };
                    }
                } catch (JSONException e) {
                }
            }

            return new Object[] { false, sb.toString() };
        } catch (Exception e) {
            return new Object[] { false, "Unknown error" };
        }
    }

    @Override
    protected void onPostExecute(Object[] result) {
        activity.completeSave(((Boolean)result[0]).booleanValue(), (String)result[1]);
    }
}
