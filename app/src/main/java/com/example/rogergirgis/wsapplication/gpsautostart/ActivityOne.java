package com.example.rogergirgis.wsapplication.gpsautostart;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rogergirgis.wsapplication.R;

public class ActivityOne extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissionIfNotAlreadyGranted(Manifest.permission.ACCESS_FINE_LOCATION, 101);
        getPermissionIfNotAlreadyGranted(Manifest.permission.INTERNET, 102);

        //Button button_toActivityTwo = findViewById(R.id.button_toActivityTwo);
        Button button_toActivityTwo = null;
        button_toActivityTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ActivityTwo.class);
                startActivity(intent);
            }
        });

        //Button button_startService = findViewById(R.id.button_startService);
        Button button_startService = null;
        button_startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), GPSService.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.ACTIVE_ACTIVITY, Constants.MAIN_ACITIVTY);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.ACTIVE_ACTIVITY, Constants.ACTIVITY_PAUSED);
        editor.commit();
    }

    public void getPermissionIfNotAlreadyGranted(String permission, int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }
}
