package com.example.rogergirgis.wsapplication.gpsautostart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.rogergirgis.wsapplication.MainActivity;
import com.example.rogergirgis.wsapplication.R;

public class ActivityTwo extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        //setContentView(R.layout.activity_two);

        //Button button_toMainActivity = findViewById(R.id.button_toMainActivity);
        Button button_toMainActivity = null;
        button_toMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        //Button button_stopService = findViewById(R.id.button_stopService);
        Button button_stopService = null;
        button_stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), GPSService.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.ACTIVE_ACTIVITY, Constants.ACTIVITY_TWO);
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
}
