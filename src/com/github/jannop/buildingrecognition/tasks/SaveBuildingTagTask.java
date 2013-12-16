package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.WebRequest;
import com.github.jannop.buildingrecognition.activities.EditLocationActivity;
import org.json.JSONObject;

public class SaveBuildingTagTask extends AsyncTask<Object, Void, Object[]> {
    private final EditLocationActivity activity;

    public SaveBuildingTagTask(EditLocationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        BuildingDetails building = (BuildingDetails)params[0];
        String username = (String)params[1];

        WebRequest request = new WebRequest("http://bldrecog.appspot.com/addtag");
        JSONObject result = request.post("username=" + username + "&building=" + building.id + "&name=" + building.name);

        boolean isSuccess = result.optBoolean("success", false);
        String message = result.optString("message", "");

        return new Object[] { isSuccess, message };
    }

    @Override
    protected void onPostExecute(Object[] result) {
        activity.completeSave(((Boolean)result[0]), (String)result[1]);
    }
}
