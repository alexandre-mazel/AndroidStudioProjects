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
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private String mstrStatus; // used to refresh in the good thread
    private int mnBpm;

    private int mnNbrUpdateBpm;
    private String mstrBpmLastTxt;
    private String mstrButtonOriginalReconnectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");
        Log.v("DBG", "DisplaySensorActivity -- Create");
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setDisplayActivity( this );

        mstrButtonOriginalReconnectText = "";

    } // onCreate

    protected void createDisplaySensorWidgets(int v)
    {
        setContentView( v );

        mTxtBpm = (TextView) findViewById(R.id.menu_bpm);
        maTxtAngle = new TextView[mnNbrAngle];
        maTxtAngle[0] = (TextView) findViewById(R.id.menu_angle1);
        maTxtAngle[1] = (TextView) findViewById(R.id.menu_angle2);
        maTxtAngle[2] = (TextView) findViewById(R.id.menu_angle3);
        mTxtDeviceStatus = (TextView) findViewById(R.id.menu_txt_device_status);
        mTxtDeviceInfo = (TextView) findViewById(R.id.menu_txt_device_info);

        postRefreshDisplayInterface(2000);
    }

    public void connect(View view)
    {
        // connect to previously discovered and calibrated sensors
        Global.getAngularManager().getDetectedSensorNbr(); // to just create the singleton
        Global.getCurrentSensorsManager().setKnownSensors(Global.getAngularManager().getKnownSensors());

        postConnectBLE(10);
        //Global.getCurrentSensorsManager().discover();

    }

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

            Global.getCurrentSensorsManager().discover();
            postRefreshBLE(1000);
        }
        Log.v("DBG", "start BLE stuffs - end");
    }



    protected void postConnectBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                DisplaySensorActivity.this.connectBLE();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    private void refreshBLE() {
        //Log.v("DBG", "refreshBLE");
        Global.getCurrentSensorsManager().update();
        postRefreshBLE( 1000 );
        //Log.v("DBG", "refreshBLE - end");
    }

    private void postRefreshBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                DisplaySensorActivity.this.refreshBLE();
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
        String currentDateAndTime = sdf.format(new Date());
        String newLine = currentDateAndTime + ": " + String.valueOf(nBpm);
        Log.v( "DBG", newLine );
        mstrBpmLastTxt += newLine + "\n";

        mnNbrUpdateBpm += 1;
        if( mnNbrUpdateBpm > 60 ) {
            try{
                Log.v("DBG", "updateBpm: outputting to file!!!");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, "alex_hr.txt");

                FileOutputStream fOut = new FileOutputStream(file, true ); // true for append
                fOut.write(mstrBpmLastTxt.getBytes());
                fOut.close();
            }
            catch (Exception e){
                Log.v("DBG", "updateBpm: Exception: disk error: " + e.toString());
            }
            mnNbrUpdateBpm = 0;
            mstrBpmLastTxt = "";
        }

    }
    public void updateAngle(String strDeviceName, double rAngle)
    {
        //mTxtBpm.setText(String.valueOf(nBpm));
        //mrAngle = rAngle; // to be refreshed later
        rAngle -= 17.9; // results: 161.6 / 162.0 / 162.3 / 160.2 / 158.6 / 153.8 / 168.1 / 168.4 / 168.0 / 166.5 / 167.3 / 165.6 / 167.0 / 167.0 / 165.1 / 167.
        int nCurrentIdx = Global.getSensorIdx(strDeviceName);
        //maAngleAverage[nCurrentIdx].addValue(rAngle);
        //marAngle[nCurrentIdx] = maAngleAverage[nCurrentIdx].computeAverage().doubleValue();
    }

    protected void refreshDisplayInterface(Boolean bAutoRepost) {
        //Log.v("DBG", "in refreshBpm update !!!: mnBpm:" + mnBpm);
        //Log.d("DBG", "DisplaySensorActivity.refreshDisplayInterface: entering..." );
        if( mstrStatus != null ) {
            mTxtDeviceStatus.setText(mstrStatus);
            mstrStatus = null; // could miss one from time to time
        }
        if( mnBpm != 0 ) {
            mTxtBpm.setText(String.valueOf(mnBpm));
            mnBpm = 0; // could miss one from time to time
        }

        if( mTxtDeviceInfo == null ) {
            Log.d("DBG", "refreshDisplayInterface: the interface hasn't been created ?!?" );
            return;
        }

        mTxtDeviceInfo.setText( "Known: " + Global.getAngularManager().getDetectedSensorNbr() + "\n" );

        for( int i = 0; i < mnNbrAngle; ++i) {
            double rAngle = Global.getAngularManager().getAngle(i);
            double timeLastUpdate = Global.getAngularManager().getLastUpdate(i);


            if( System.currentTimeMillis() - timeLastUpdate > 2000 )
            {
                maTxtAngle[i].setTextColor(Color.RED);
            }
            else
            {
                maTxtAngle[i].setTextColor(Color.BLACK);
                maTxtAngle[i].setText(String.format("%.1f", rAngle) + "°");
            }
        }

        if( Global.getCurrentSensorsManager().isScanning() )
        {
            Button recoButton = (Button) findViewById(R.id.menu_reconnect);

            if( mstrButtonOriginalReconnectText.equals("") ) {
                mstrButtonOriginalReconnectText = recoButton.getText().toString();
            }

            String strText = mstrButtonOriginalReconnectText;
            long nNbrPoint = System.currentTimeMillis()/1000;
            nNbrPoint = Global.modulo(nNbrPoint,4);

            //Log.d("DBG", "nNbrPoint: " + nNbrPoint );

            for(int i = 0; i < nNbrPoint; ++i ) {
                strText += ".";
            }
            
            recoButton.setText(strText);
        }

        if( bAutoRepost ) {
            postRefreshDisplayInterface(500);
        }

    }

    private void postRefreshDisplayInterface(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                DisplaySensorActivity.this.refreshDisplayInterface(true);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void onBack( View view )
    {

    }

}
