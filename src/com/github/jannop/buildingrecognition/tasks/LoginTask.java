package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.WebRequest;
import com.github.jannop.buildingrecognition.activities.LoginActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginTask extends AsyncTask<String, Void, Boolean> {
    private final LoginActivity activity;
    private String username;

    public LoginTask(LoginActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        username = params[0];

        WebRequest request = new WebRequest("http://bldrecog.appspot.com/login?username=" + username);
        JSONObject result = request.execute();

        return getBooleanProperty(result, "success");
    }

    @Override
    protected void onPostExecute(Boolean success) {
        activity.completeLogin(success.booleanValue(), username);
    }

    private static boolean getBooleanProperty(JSONObject object, String propertyName) {
        try {
            if (object.has(propertyName))
                return object.getBoolean(propertyName);
        } catch (JSONException e) {
        }
        return false;
    }
}
