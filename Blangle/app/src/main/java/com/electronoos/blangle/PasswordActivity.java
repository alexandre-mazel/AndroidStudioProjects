package com.electronoos.blangle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.electronoos.blangle.util.Averager;

/**
 * Created by a on 14/02/17.
 */
public class PasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity(this);

        setContentView(R.layout.activity_password);
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



    public void onSettingsCalibration(View view) {

    }

    public void onSettingsDiscrimination(View view) {
        Toast.makeText(this, R.string.settings_txt_move_first_one_then_second_then_third, Toast.LENGTH_LONG).show();
        mMode = 2;
        for( int i = 0; i < Global.getAngularManager().getDetectedSensorNbr(); ++i) {
            marAngleRef[i] = Global.getAngularManager().getAngle(i);
        }
        postUpdateAngleTimed(500);
        //Toast.makeText(this, R.string.txt_done, Toast.LENGTH_LONG).show();
        //mMode = 0;
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }


}
