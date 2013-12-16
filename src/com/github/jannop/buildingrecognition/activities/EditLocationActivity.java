package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.github.jannop.buildingrecognition.BuildingDetails;
import com.github.jannop.buildingrecognition.R;
import com.github.jannop.buildingrecognition.tasks.SaveBuildingTagTask;

public class EditLocationActivity extends Activity {
    private BuildingDetails building;
    private String username;

    private EditText editBuildingName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        building = (BuildingDetails)getIntent().getSerializableExtra("building");
        username = getIntent().getStringExtra("username");

        editBuildingName = (EditText)findViewById(R.id.editBuildingName);
        editBuildingName.setText(building.name);

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
        building.name = editBuildingName.getText().toString();
        new SaveBuildingTagTask(this).execute(building, username);
    }

    public void completeSave(boolean success, String message) {
        if (success) {
            Bundle conData = new Bundle();
            conData.putSerializable("building", building);
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            showSaveFailure(message);
            setResult(RESULT_CANCELED);
        }
    }

    private void showSaveFailure(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Login Failed");
        alertDialog.setMessage("Could not log in with specified username. " + message);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editBuildingName.setEnabled(true);
            }
        });
        alertDialog.show();
    }
}
