package com.example.rogergirgis.wsapplication.gpsautostart;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.rogergirgis.wsapplication.MainActivity;
import com.example.rogergirgis.wsapplication.WSActivity;
import com.google.gson.Gson;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.example.rogergirgis.wsapplication.gpsautostart.Constants.GPS_SERVICE_RUNNING;

public class GPSService extends Service {
    // max time to spend in WS ACTIVITY crossing intersection
    private final int MAX_WS_ACTIVITY_TIME_MS = 10*1000;
    // millis since last start of WS ACTIVITY
    private volatile long timeLastStartedWSActivity = System.currentTimeMillis();
    // to avoid re-initializing any state ifOnStartCommand() is called redundantly
    private volatile boolean alreadyStartedOnce = false;

    private LocationListener ll;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // not binding, only starting
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (alreadyStartedOnce) return START_STICKY;
        alreadyStartedOnce = true;

        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(GPS_SERVICE_RUNNING, true);
        editor.commit();

        // set up a listener for gps
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location2) {
                boolean gpsServiceIsRunning =
                        getApplicationContext()
                                .getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE)
                                .getBoolean(GPS_SERVICE_RUNNING, false); // defaultValue=false
                //if (!gpsServiceIsRunning) return;

                final Location location = location2;

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // get lat lon radius
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            int radius = 8;

                            // TODO: remove test values
                            lat = 45.502239;
                            lon = -73.577248;
                            radius = 100;
                            // TODO: remove test values

                            // request intersection data
                            String url =
                                    "https://isasdev.cim.mcgill.ca:44343/autour/getPlaces.php"+
                                    "?framed=1&times=1"+
                                    "&radius="+radius+"&lat="+lat+"&lon="+lon+
                                    "&condensed=0&from=osmxing&as=json&font=9&pad=0";
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpRequest request = new HttpGet(url);
                            HttpResponse response = httpClient.execute((HttpUriRequest) request);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()
                            ));

                            // parse intersection data
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = "";
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                            String responseString = stringBuilder.toString();

                            Gson gson = new Gson();
                            RequestResult rr = gson.fromJson(responseString, RequestResult.class);
                            RequestResult.IntersectionData[] intersectionData = rr.results;

                            // respond to contents of intersection data
                            boolean inRangeOfIntersection = false;
                            if (intersectionData.length > 0) {
                                inRangeOfIntersection = true;
                            }

                            // if near an intersection and running MainActivity, switch to WSActivity
                            // if not near an intersection and running WSActivity, switch to MainActivity
                            String runningActivity = getApplicationContext().getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE)
                                    .getString(Constants.ACTIVE_ACTIVITY, "defaultValue");
                            if (inRangeOfIntersection && !runningActivity.equals(Constants.WS_ACTIVITY)) {
                                // start WSActivity.class
                                timeLastStartedWSActivity = System.currentTimeMillis();
                                Intent intent = new Intent(getApplicationContext(), WSActivity.class);
                                intent.putExtra(Constants.DO_TUTORIAL, false);
                                startActivity(intent);
                            } else if ((!inRangeOfIntersection && runningActivity.equals(Constants.WS_ACTIVITY))
                                    || ( runningActivity.equals(Constants.WS_ACTIVITY) &&
                                    System.currentTimeMillis() - timeLastStartedWSActivity > MAX_WS_ACTIVITY_TIME_MS )
                            ) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            @Override
            public void onProviderEnabled(String s) { }
            @Override
            public void onProviderDisabled(String s) { }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // request location updates from the gps provider every 3000 ms, even if travelled 0 distance
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, ll);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(ll);
    }
}

// for parsing json into a convenient java object
class RequestResult {
    IntersectionData[] results;
    String[] footer;

    static class IntersectionData {
        String id;
        double[] ll; // lat lon
        int cat;
        String title; // intersection name in natural language (english)
        String from; // data source
        int rating; // expected quality of data, 1-10 scale
    }
}
