package com.electronoos.blangle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.electronoos.blangle.util.Averager;

import java.util.ArrayList;

/**
 * Created by a on 14/02/17.
 */
public class DiscoverActivity extends DisplaySensorActivity {


    private Integer mMode = 0; // 0: in nothing, 1: in calib, 2: in discrimination

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");
        Log.v("DBG", "DiscoverActivity -- Create");
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_discover);

        createDisplaySensorWidgets( R.layout.activity_discover );

        // in discover, we erase all previous data and sensor narrowing
        Global.getAngularManager().reset();
        Global.getCurrentSensorsManager().setKnownSensors( new ArrayList <String>() );
    }

    @Override
    public void onButtonReconnect(View view) {
        // in discover, we erase all previous data and sensor narrowing
        Global.getAngularManager().reset();
        Global.getCurrentSensorsManager().setKnownSensors( new ArrayList <String>() );

        super.onButtonReconnect(view);
    }

    @Override
    protected void onPause() {
        // we want this application to be stopped when set on background
        Log.v("DBG", "SettingsActivity: ------------------------------ onPause...");
        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.v("DBG", "SettingsActivity: ------------------------------ onStop...");
        super.onStop();
        finish();
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}
