package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.github.jannop.buildingrecognition.R;

public class LoginActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onSignIn(View view) {
        // TODO : Login via app engine

        // TODO : Alert when no network connection (required for app engine communication
        // and map display).

        Intent intent = new Intent(getApplicationContext(), ShowLocationActivity.class);
        intent.putExtra("activity", "login");
        startActivity(intent);

        // TODO : After successful login retrieve user location
        // Depending on result open next activity which either
        // shows detected location or asks to choose building from
        // the map.

        finish();
    }
}
