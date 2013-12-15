package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.github.jannop.buildingrecognition.R;

public class EditLocationActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
    }

    public void onDiscard(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {

    }
}
