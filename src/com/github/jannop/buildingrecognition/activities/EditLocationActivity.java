package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.R;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditLocationActivity extends Activity {
    private BuildingDetails building;

    private EditText editBuildingName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        building = (BuildingDetails)getIntent().getSerializableExtra("building");

        editBuildingName = (EditText)findViewById(R.id.editBuildingName);

        TextView txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtAddress.setText(building.address);

        if (building.name != null) {
            editBuildingName.setText(building.name);
        }
    }

    public void onDiscard(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {
        editBuildingName.setEnabled(false);
        try {
            URL url = new URL("http://bldrecog.appspot.com/addtag");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Content-Type", "text/plain");
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write("username=&building=&name=");
            out.close();

            /*
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println(""+sb.toString());

            }else{
                System.out.println(urlConnection.getResponseMessage());
            }*/

            urlConnection.disconnect();
        } catch (Exception e) {

        }
        editBuildingName.setEnabled(true);
    }
}
