package com.electronoos.blangle;

import com.electronoos.blangle.util.Averager;
import com.electronoos.blangle.util.GetUserInput;
import com.electronoos.blangle.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Abstract class to draw update from sensor management (should have ABC in the class name, but not compatible ?)
 * But don't handle the activity part, just the feedback and txt update
 */
public class DisplaySensorActivity extends Activity {

    final int mnNbrAngle = 3;
    private TextView mTxtBpm;
    private TextView[] maTxtAngle;
    private TextView mTxtDeviceStatus;
    private TextView mTxtDeviceInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setDisplayActivity( this );


        mTxtBpm = (TextView) findViewById(R.id.menu_bpm);
        maTxtAngle = new TextView[mnNbrAngle];
        maTxtAngle[0] = (TextView) findViewById(R.id.menu_angle1);
        maTxtAngle[1] = (TextView) findViewById(R.id.menu_angle2);
        maTxtAngle[2] = (TextView) findViewById(R.id.menu_angle3);
        mTxtDeviceStatus = (TextView) findViewById(R.id.menu_txt_device_status);
        mTxtDeviceInfo = (TextView) findViewById(R.id.menu_txt_device_info);
    } // onCreate

    public void onChoiceCalc(View view) {
        // Kabloey
    }


    public void onButtonReconnect(View view) {
        postConnectBLE(10);
    }

    private void connectBLE() {
        Log.v("DBG", "start BLE stuffs");
        mTxtDeviceStatus.setText("Searching...");
        if( true ) {
            if( Global.getCurrentSensorsManager() == null ) {
                mSensorsManager = new SensorsManager();
                Global.setCurrentSensorsManager(mSensorsManager);
                mSensorsManager.init();
            }
            mSensorsManager.discover();
            postRefreshBLE(1000);
        }
        if( false ) {
            Intent myIntent = new Intent(Menu.this, DeviceScanActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            Menu.this.startActivity(myIntent);
        }
        Log.v("DBG", "start BLE stuffs - end");
    }



    private void postConnectBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.connectBLE();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    private void refreshBLE() {
        //Log.v("DBG", "refreshBLE");
        if( mSensorsManager != null )
            mSensorsManager.update();
        postRefreshBLE( 1000 );
        //Log.v("DBG", "refreshBLE - end");
    }

    private void postRefreshBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.refreshBLE();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void updateStatus(String strStatus) {
        mstrStatus = strStatus;
    }

    public void updateBpm(int nBpm)
    {
        //Log.v("DBG", "in txtview update !!!: " + nBpm);
        //mTxtBpm.setText(String.valueOf(nBpm));
        mnBpm = nBpm; // to be refreshed later
        //postRefreshBpm( 10 ); // Can't create handler inside thread that has not called Looper.prepare()

        // output to file
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        String newLine = currentDateandTime + ": " + String.valueOf(nBpm);
        Log.v( "DBG", newLine );
        mstrLastTxt += newLine + "\n";

        mnNbrUpdateBpm += 1;
        if( mnNbrUpdateBpm > 60 ) {
            try{
                Log.v("DBG", "updateBpm: outputting to file!!!");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, "alex_hr.txt");

                FileOutputStream fOut = new FileOutputStream(file, true ); // true for append
                fOut.write(mstrLastTxt.getBytes());
                fOut.close();
            }
            catch (Exception e){
                Log.v("DBG", "updateBpm: Exception: disk error: " + e.toString());
            }
            mnNbrUpdateBpm = 0;
            mstrLastTxt = "";
        }

    }
    public void updateAngle(String strDeviceName, double rAngle)
    {
        //mTxtBpm.setText(String.valueOf(nBpm));
        //mrAngle = rAngle; // to be refreshed later
        rAngle -= 17.9; // results: 161.6 / 162.0 / 162.3 / 160.2 / 158.6 / 153.8 / 168.1 / 168.4 / 168.0 / 166.5 / 167.3 / 165.6 / 167.0 / 167.0 / 165.1 / 167.
        int nCurrentIdx = Global.getSensorIdx(strDeviceName);
        maAngleAverage[nCurrentIdx].addValue(rAngle);
        marAngle[nCurrentIdx] = maAngleAverage[nCurrentIdx].computeAverage().doubleValue();
    }

    private void refreshInterface() {
        //Log.v("DBG", "in refreshBpm update !!!: mnBpm:" + mnBpm);
        if( mstrStatus != null ) {
            mTxtDeviceStatus.setText(mstrStatus);
            mstrStatus = null; // could miss one from time to time
        }
        if( mnBpm != 0 ) {
            mTxtBpm.setText(String.valueOf(mnBpm));
            mnBpm = 0; // could miss one from time to time
        }
        for( int i = 0; i < mnNbrAngle; ++i) {
            if (marAngle[i] != 0.) {
                maTxtAngle[i].setText(String.format("%.1f", marAngle[i]) + "°");
                marAngle[i] = 0.; // could miss one from time to time
                if( maTimeLastUpdateMs[i] <= -1 ) {
                    maTxtAngle[i].setTextColor(Color.BLACK);
                }
                maTimeLastUpdateMs[i] = System.currentTimeMillis();
            }
            else
            {
                if( maTimeLastUpdateMs[i] > -1 )
                {
                    if( System.currentTimeMillis() - maTimeLastUpdateMs[i] > 2000 ) {
                        maTimeLastUpdateMs[i] = -1;
                        maTxtAngle[i].setTextColor(Color.RED);
                    }
                }
            }
        }

        postRefreshInterface(500);

    }

    private void postRefreshInterface(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.refreshInterface();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void onBack( View view )
    {

    }

}
