package com.github.jannop.buildingrecognition.tasks;

import android.os.AsyncTask;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.WebRequest;
import com.github.jannop.buildingrecognition.activities.ShowLocationActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class DetectBuildingTask extends AsyncTask<GeoPoint, Void, BuildingDetails> {
    private final ShowLocationActivity activity;
    private final boolean detected;

    public DetectBuildingTask(ShowLocationActivity activity, boolean detected) {
        this.activity = activity;
        this.detected = detected;
    }

    @Override
    protected BuildingDetails doInBackground(GeoPoint... params) {
        GeoPoint location = params[0];

        if (location == null) {
            return null;
        }

        String address = "http://bldrecog.appspot.com/check_location?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        WebRequest request = new WebRequest(address);
        JSONObject result = request.get();

        return parseBuildingDetails(result);
    }

    @Override
    protected void onPostExecute(BuildingDetails building) {
        activity.completeBuildingDetection(building, detected);
    }

    private static BuildingDetails parseBuildingDetails(JSONObject details) {
        if (details == null) {
            return null;
        }

        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

        JSONArray array = details.optJSONArray("building_nodes");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONArray coordinate = array.optJSONArray(i);
                if (coordinate != null) {
                    points.add(new GeoPoint(coordinate.optDouble(0), coordinate.optDouble(1)));
                }
            }
        }

        if (points.size() < 1) {
            return null;
        }

        BuildingDetails buildingDetails = new BuildingDetails();
        buildingDetails.address = getAddressString(details);
        buildingDetails.polygon = points;
        buildingDetails.id = details.optLong("building_id", 0);

        return buildingDetails;
    }

    private static String getAddressString(JSONObject building) {
        if (building == null)
            return null;

        StringBuilder sb = new StringBuilder();

        String streetName = building.optString("addr:street");
        if (streetName != null && !streetName.isEmpty())
            sb.append(streetName);

        String houseNumber = building.optString("addr:housenumber");
        if (houseNumber != null && !houseNumber.isEmpty())
            sb.append((sb.length() > 0 ? " - " : "") + houseNumber);

        String cityName = building.optString("addr:city");
        if (cityName != null && !cityName.isEmpty())
            sb.append((sb.length() > 0 ? ", " : "") + cityName);

        String countryCode = building.optString("addr:country");
        if (countryCode != null && !countryCode.isEmpty())
            sb.append((sb.length() > 0 ? " " : "") + "(" + countryCode + ")");

        return sb.toString();
    }
}
