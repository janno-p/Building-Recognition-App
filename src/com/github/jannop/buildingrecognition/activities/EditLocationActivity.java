package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.R;

public class EditLocationActivity extends Activity {
    private BuildingDetails building;

    private EditText editName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        building = (BuildingDetails)getIntent().getSerializableExtra("building");

        editName = (EditText)findViewById(R.id.editText);

        TextView txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtAddress.setText(building.address);

        if (building.name != null) {
            editName.setText(building.name);
        }
    }

    public void onDiscard(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {

    }
}
