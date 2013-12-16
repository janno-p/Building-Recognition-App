package com.github.jannop.buildingrecognition.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.github.jannop.buildingrecognition.R;
import com.github.jannop.buildingrecognition.tasks.LoginTask;

public class LoginActivity extends Activity {
    private EditText editUsername;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = (EditText)findViewById(R.id.editText);
    }

    public void onSignIn(View view) {
        editUsername.setEnabled(false);
        new LoginTask(this).execute(editUsername.getText().toString());

        // TODO : Alert when no network connection (required for app engine communication
        // and map display).
    }

    public void completeLogin(boolean success, String username) {
        if (success) {
            Intent intent = new Intent(getApplicationContext(), ShowLocationActivity.class);
            intent.putExtra("activity", "login");
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            showLoginFailure();
        }
    }

    private void showLoginFailure() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Login Failed");
        alertDialog.setMessage("Could not log in with specified username.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editUsername.setEnabled(true);
            }
        });
        alertDialog.show();
    }
}
