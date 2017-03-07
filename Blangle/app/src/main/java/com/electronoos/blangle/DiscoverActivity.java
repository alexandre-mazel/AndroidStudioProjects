package com.electronoos.blangle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.electronoos.blangle.util.Averager;

/**
 * Created by a on 14/02/17.
 */
public class SettingsActivity extends Activity {

    final int mnNbrAngle = 3;
    private Averager[] maAngleAverage;
    private double[] marAngle;

    private Integer mMode = 0; // 0: in nothing, 1: in calib, 2: in discrimination

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity(this);

        setContentView(R.layout.activity_settings);

        marAngle = new double[mnNbrAngle];
        maAngleAverage = new Averager[mnNbrAngle];
        for( int i = 0; i < mnNbrAngle; ++i) {
            Log.v("DBG", "i: " + i );
            maAngleAverage[i] = new Averager<Double>(30);
        }

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
        mMode = 1;
        Toast.makeText(this, R.string.settings_txt_place_sensor_flat_and_dont_move, Toast.LENGTH_LONG).show();
        // TODO: faire un post d'une fin d'update!
        for( int i = 0; i < 20; ++i) {
            Global.getCurrentSensorsManager().update(); // not usefull
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//        for( int i = 0; i < mnNbrAngle; ++i) {
//            Log.v( "DBG: SettingsActiviy", "val: " + marAngle[i] );
//        }
        Global.getAngularManager().calibrateAll();
        Toast.makeText(this, R.string.txt_done, Toast.LENGTH_SHORT).show();
        mMode = 0;
    }

    public void onSettingsDiscrimination(View view) {
        Toast.makeText(this, R.string.settings_txt_move_first_one_then_second_then_third, Toast.LENGTH_LONG).show();
        mMode = 2;
        Toast.makeText(this, R.string.txt_done, Toast.LENGTH_SHORT).show();
        mMode = 0;
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void updateAngle(String strDeviceName, double rAngle)
    {
        int nCurrentIdx = Global.getSensorIdx(strDeviceName);
        maAngleAverage[nCurrentIdx].addValue(rAngle);
        marAngle[nCurrentIdx] = maAngleAverage[nCurrentIdx].computeAverage().doubleValue();
    }

}
