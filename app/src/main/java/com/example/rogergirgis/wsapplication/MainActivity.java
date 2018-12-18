package com.example.rogergirgis.wsapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.rogergirgis.wsapplication.gpsautostart.Constants;
import com.example.rogergirgis.wsapplication.gpsautostart.GPSService;

import static com.example.rogergirgis.wsapplication.gpsautostart.Constants.GPS_SERVICE_RUNNING;

public class MainActivity extends AppCompatActivity {

    int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get all necessary permissions up front
        getPermissionIfNotAlreadyGranted(Manifest.permission.INTERNET, 100);
        getPermissionIfNotAlreadyGranted(Manifest.permission.ACCESS_COARSE_LOCATION, 101);
        getPermissionIfNotAlreadyGranted(Manifest.permission.ACCESS_FINE_LOCATION, 102);

        Button btnExperiment2 = (Button) findViewById(R.id.btn_experiment2);
        btnExperiment2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent experimentIntent = new Intent(MainActivity.this, WSActivity.class);
                Bundle configuration = new Bundle();

                configuration.putInt(WSActivity.FOLDER_KEY, clickCount);
                clickCount++;
                experimentIntent.putExtras(configuration);
                MainActivity.this.startActivity(experimentIntent);
            }
        });

        Button button_killService = findViewById(R.id.button_killService);
        button_killService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean gpsServiceIsRunning =
                    getApplicationContext()
                        .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_MULTI_PROCESS)
                        .getBoolean(GPS_SERVICE_RUNNING, false); // defaultValue=false
                if (gpsServiceIsRunning) {
                    stopService(new Intent(getApplicationContext(), GPSService.class));
                }
            }
        });

        Button button_pauseService = findViewById(R.id.button_pauseService);
        button_pauseService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean gpsServiceIsRunning =
                        getApplicationContext()
                                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_MULTI_PROCESS)
                                .getBoolean(GPS_SERVICE_RUNNING, false); // defaultValue=false

                SharedPreferences sharedPreferences = getApplicationContext()
                        .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (gpsServiceIsRunning) {
                    editor.putBoolean(Constants.GPS_SERVICE_RUNNING, false);
                } else {
                    editor.putBoolean(Constants.GPS_SERVICE_RUNNING, true);
                }
                editor.commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.ACTIVE_ACTIVITY, Constants.MAIN_ACTIVITY);
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
