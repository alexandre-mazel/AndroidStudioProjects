package com.electronoos.blangle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by a on 14/02/17.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity(this);

        setContentView(R.layout.activity_menu);
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
    }

    public void onSettingsCalibration(View view) {
        Toast.makeText(this, R.string.settings_txt_place_sensor_flat_and_dont_move, Toast.LENGTH_LONG).show();
        Toast.makeText(this, R.string.txt_done, Toast.LENGTH_SHORT).show();
    }

    public void onSettingsDiscrimination(View view) {
        Toast.makeText(this, R.string.settings_txt_move_first_one_then_second_then_third, Toast.LENGTH_LONG).show();
        Toast.makeText(this, R.string.txt_done, Toast.LENGTH_SHORT).show();
    }

}
