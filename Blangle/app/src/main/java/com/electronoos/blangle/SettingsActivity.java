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
public class SettingsActivity extends Activity {

    final int mnNbrAngle = 3;
    private Averager[] maAngleAverage;
    private double[] marAngle;
    private double[] marAngleRef; // for ref and identification

    private Integer mMode = 0; // 0: in nothing, 1: in calib, 2: in discrimination

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity(this);

        setContentView(R.layout.activity_settings);
/*
        marAngle = new double[mnNbrAngle];
        maAngleAverage = new Averager[mnNbrAngle];
        for( int i = 0; i < mnNbrAngle; ++i) {
            Log.v("DBG", "i: " + i );
            maAngleAverage[i] = new Averager<Double>(30);
        }
*/
        marAngleRef = new double[mnNbrAngle];
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

    public void onSettingsDiscover(View view) {
        Intent intent = new Intent(this, DiscoverActivity.class);
        startActivity(intent);
    }

    public void onSettingsCalibration(View view) {
        mMode = 1;
        Toast.makeText(this, R.string.settings_txt_place_sensor_flat_and_dont_move, Toast.LENGTH_LONG).show();
/*
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
*/
//        for( int i = 0; i < mnNbrAngle; ++i) {
//            Log.v( "DBG: SettingsActiviy", "val: " + marAngle[i] );
//        }
        Global.getAngularManager().calibrateAll();
        Toast.makeText(this, R.string.txt_done, Toast.LENGTH_LONG).show();
        mMode = 0;
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

    public void updateAngle(String strDeviceName, double rAngle)
    {
        Log.v("DBG", "in updateAngle (not used)" );
/*
        int nCurrentIdx = Global.getSensorIdx(strDeviceName);
        maAngleAverage[nCurrentIdx].addValue(rAngle);
        marAngle[nCurrentIdx] = maAngleAverage[nCurrentIdx].computeAverage().doubleValue();
        if( mMode == 2 )
        {
            if( Math.abs(marAngle[nCurrentIdx]-marAngleRef[nCurrentIdx]) > 90 )
            {
                Toast.makeText(this, "order next is: "  + strDeviceName, Toast.LENGTH_LONG).show();

                boolean bAllSeen = Global.getAngularManager().setOrderNext( strDeviceName );
                if( bAllSeen ) {
                    mMode = 0;
                    Toast.makeText(this, "finito! ", Toast.LENGTH_LONG).show();
                }
            }
        }
*/
    }

    //@Override
    private void updateAngleTimed() {
        Log.v("DBG", "in updateAngleTimed, mMode: " + mMode );
        if( mMode != 2 )
        {
            return;
        }

        for( int i = 0; i < Global.getAngularManager().getDetectedSensorNbr(); ++i)
        {
            if( marAngleRef[i] < -400.0 )
                continue; // already handled

            if( Math.abs(Global.getAngularManager().getAngle(i)-marAngleRef[i]) > 90 )
            {
                marAngleRef[i] = -1000.0;
                String strDeviceName = Global.getAngularManager().getName( i );

                Toast.makeText(this, "order next is: "  + strDeviceName, Toast.LENGTH_LONG).show();

                boolean bAllSeen = Global.getAngularManager().setOrderNext( strDeviceName );
                if( bAllSeen ) {
                    mMode = 0;
                    Toast.makeText(this, "finito! ", Toast.LENGTH_LONG).show();
                }
            }
        }

        postUpdateAngleTimed(300);

    }

    private void postUpdateAngleTimed(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                SettingsActivity.this.updateAngleTimed();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

}
