package com.github.jannop.buildingrecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onSignIn(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowLocationActivity.class);
        startActivity(intent);
    }
}
