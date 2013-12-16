package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.WebRequest;
import com.github.jannop.buildingrecognition.activities.LoginActivity;
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
        JSONObject result = request.get();

        return result.optBoolean("success", false);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        activity.completeLogin(success.booleanValue(), username);
    }
}
