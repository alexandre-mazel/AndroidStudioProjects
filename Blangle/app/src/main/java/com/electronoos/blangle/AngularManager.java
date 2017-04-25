package com.electronoos.blangle;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.electronoos.blangle.util.Averager;
import com.electronoos.blangle.util.DataLogger;
import com.electronoos.blangle.util.EKF;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Handle all stuffs needed to manage a bunch of sensors (mainly angular one, using BTLE)
 * Created by a on 19/02/17.
 */
public class AngularManager {

    private int nNbrSensors_;
    private int nNbrValueToAverage_;

    // for each sensors:
    private Averager[] aAngleAverager_; // you can choose "on the fly" wich method you want.
    private EKF[] aAngleEKF_;
    private DataLogger[] aAngleLogger_;

    private double[] arAngle_; // Last measured/averaged angle

    private double[] arOffset_;

    private double[] aTimeLastUpdateMs_;

    private Hashtable<String,Integer> sensorTable_; // a way to associate a sensor string to an idx (idx is then the index of above list)

    private String[] aDeviceOrder_; // the name of the device ordered 0 (the first sensor) => name of the sensor ... (empty if unset)

    File pathForConfig_;


    // nNbrValueToAverage => in BTLE each second gives 10 values... so 20 to 30 is ok
    public AngularManager( int nNbrSensors, int nNbrValueToAverage )
    {
        nNbrSensors_ = nNbrSensors;
        nNbrValueToAverage_ = nNbrValueToAverage;
        reset();


        //File pathForConfig_ = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File pathForConfig_ = Context.getCacheDir() + File.separator;
        File pathForConfig_ = Environment.getDataDirectory();

        readConfig();
    }

    public void reset()
    {
        Log.d( "DBG", "AngularManager.reset: begin..." );
        aAngleAverager_ = new Averager[nNbrSensors_];
        aAngleEKF_ = new EKF[nNbrSensors_];
        aAngleLogger_ = new DataLogger[nNbrSensors_];
        arAngle_ = new double[nNbrSensors_];
        arOffset_= new double[nNbrSensors_];
        aTimeLastUpdateMs_ = new double[nNbrSensors_];
        aDeviceOrder_ = new String[nNbrSensors_];

        for (int i = 0; i < nNbrSensors_; ++i) {
            aAngleAverager_[i] = new Averager<Double>(nNbrValueToAverage_);
            aAngleEKF_[i] = new EKF();
            aAngleLogger_[i] = new DataLogger<Double>( "angle_" + String.valueOf(i) );
            arAngle_[i] = -1;
            arOffset_[i] = 0;
            aDeviceOrder_[i] = "";

        }
        sensorTable_ = new Hashtable<String, Integer>();

        Log.d( "DBG", "AngularManager.reset: getDetectedSensorNbr: " + getDetectedSensorNbr() );
    }

    public void drawDebug()
    {
        Log.d( "DBG", "AngularManager.drawDebug: getDetectedSensorNbr: " + getDetectedSensorNbr() );
        for (int i = 0; i < getDetectedSensorNbr(); ++i) {
            Log.d( "DBG", "AngularManager.drawDebug: aDeviceOrder_ " + i + ": " + aDeviceOrder_[i] );
        }
        for (String key : sensorTable_.keySet()) {
            Log.d( "DBG", "AngularManager.drawDebug: sensorTable_ " + key + ": " + sensorTable_.get(key) );
        }

    }

    private void readConfig()
    {
        Log.v("DBG", "readConfig: reading from file!!!");


        if( true )
        {
            // enable this to explode previous configuration
            //return;
        }

        try
        {
            //File file = new File( pathForConfig_, "blangle.cfg");

            //FileInputStream fIn = new FileInputStream(file );
            FileInputStream fIn = Global.getDisplayActivity().getApplicationContext().openFileInput("blangle.cfg"); //Context.MODE_PRIVATE
            DataInputStream dis = new DataInputStream( fIn );
            int n = dis.readInt();
            Log.v("DBG", "readConfig: known sensor: " + n );
            for( int i = 0; i < n; ++i )
            {
                String s = dis.readUTF();
                Double d = dis.readDouble();
                Log.v("DBG", "readConfig: " + s + ": " + d );
                arOffset_[sensorTable_.size()] = d;
                sensorTable_.put(s, sensorTable_.size() );
            }
            for( int i = 0; i < n; ++i )
            {
                String s = dis.readUTF();
                Log.v("DBG", "readConfig: order: " + s );
                aDeviceOrder_[i] = s;
            }

            dis.close();
            Log.v("DBG", "readConfig: read succeeded...");
            Log.d( "DBG", "AngularManager.read: at end: getDetectedSensorNbr: " + getDetectedSensorNbr() );

        }
        catch(FileNotFoundException fe)
        {
            Log.d("DBG", "readConfig: FileNotFoundException : " + fe);
        }
        catch(IOException ioe)
        {
            Log.d("DBG","readConfig: IOException : " + ioe);
        }
        Log.v("DBG", "readConfig: read finished...");
    }

    private void writeConfig()
    {
        Log.v("DBG", "writeConfig: outputting to file!!!");
        try
        {
            //File file = new File( pathForConfig_, "blangle.cfg");
            //FileOutputStream fOut = new FileOutputStream(file, false );
            FileOutputStream fOut = Global.getDisplayActivity().getApplicationContext().openFileOutput("blangle.cfg", Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream( fOut );
            dos.writeInt( getDetectedSensorNbr() );
            for (String key : sensorTable_.keySet())
            {
                dos.writeUTF( key );
                dos.writeDouble( arOffset_[sensorTable_.get(key)] );
            }
            for( int i = 0; i < getDetectedSensorNbr(); ++i )
            {
                dos.writeUTF(aDeviceOrder_[i]);
            }


            dos.close();
        }
        catch(FileNotFoundException fe)
        {
            Log.d("ERROR", "writeConfig: FileNotFoundException : " + fe);
        }
        catch(IOException ioe)
        {
            Log.d("ERROR","writeConfig: IOException : " + ioe);
        }
        Log.v("DBG", "writeConfig: GOOD: config wrotten");
    }


    private int getSensorIdx( String address ){
        assert( ! address.equals("") );

        if( ! sensorTable_.containsKey(address) )
        {
            sensorTable_.put(address, sensorTable_.size() );
            Log.v( "DBG", "New getSensorIdx: " + address + " => " + sensorTable_.get(address) );
        }
        int nIdx = sensorTable_.get(address);
        //Log.v( "DBG", "getSensorIdx: " + address + " => " + nIdx );
        return nIdx;
    }

    public void updateAngle(String strDeviceName, double rAngle)
    {
        //drawDebug();
        int nCurrentIdx = getSensorIdx(strDeviceName);
        aAngleAverager_[nCurrentIdx].addValue(rAngle);
        aAngleEKF_[nCurrentIdx].addValue(rAngle);
        aAngleLogger_[nCurrentIdx].addValue(rAngle-arOffset_[nCurrentIdx]);

        // choose averager or EKF
        if( true )
        {
            arAngle_[nCurrentIdx] = aAngleEKF_[nCurrentIdx].getFilteredValue();
        }
        else
        {
            arAngle_[nCurrentIdx] = aAngleAverager_[nCurrentIdx].computeAverage().doubleValue();
        }
        if( false )
        {
            arAngle_[nCurrentIdx] = rAngle; // no filter nor averager
        }
        aTimeLastUpdateMs_[nCurrentIdx] = System.currentTimeMillis();
    }

    public double getAngle( int nIdx )
    {
        return arAngle_[nIdx] - arOffset_[nIdx];
    }
    public String getName( int nIdx ) {
        // return "" if nidx unknown
        for (String key : sensorTable_.keySet()) {
            if( sensorTable_.get( key ) == nIdx )
            {
                return key;
            }
        }
        return "";
    }
    public double getLastUpdate( int nIdx )
    {
        return aTimeLastUpdateMs_[nIdx];
    }

    // return the number of detected sensors
    public int getDetectedSensorNbr(){
        return sensorTable_.size();
    }

    public ArrayList<String> getKnownSensors()
    {
        ArrayList<String> ll =  Collections.list( sensorTable_.keys() );
        return ll;
    }


    public void calibrateAll()
    {
        Log.d( "DBG", "AngularManager.calibrateAll: getDetectedSensorNbr: " + getDetectedSensorNbr() );
        for (int i = 0; i < nNbrSensors_; ++i) {
            calibrate(i);
        }
        writeConfig();
    }

    public void calibrate( int nIdx )
    {
        Log.d( "DBG", "AngularManager.calibrate: " + nIdx + ", old: " + arOffset_[nIdx] + " => " + arAngle_[nIdx] );
        arOffset_[nIdx] = arAngle_[nIdx];
    }

    // store sensors order, return true if no more sensor to ordered
    public boolean setOrderNext( String strDeviceName )
    {
        Log.d( "DBG", "AngularManager.setOrderNext: aDeviceOrder_ 0:" + aDeviceOrder_[0] + ", 1:" + aDeviceOrder_[1] + ", 2:" + aDeviceOrder_[2] );
        Log.d( "DBG", "AngularManager.setOrderNext: getDetectedSensorNbr: " + getDetectedSensorNbr() );
        for (int i = 0; i < getDetectedSensorNbr(); ++i) {
            if( aDeviceOrder_[i].equals( "" ) ) {
                aDeviceOrder_[i] = strDeviceName;
                boolean bFinished = i == getDetectedSensorNbr() - 1;
                if( bFinished) {
                    writeConfig();
                }
                return bFinished;
            }
        }
        assert( false ); // thou can't go there
        return true;
    }

    // return the angle of the sensors by ordered angle (0 => top, 1 => injection, 2 => racle)
    public double getAngleByOrderedIndex( int nIdx )
    {
        if( nIdx >= getDetectedSensorNbr() ) {
            return -1000;
        }
        try{
            if( aDeviceOrder_[nIdx] == "" )
            {
                return -2000.;
            }
            return getAngle(getSensorIdx(aDeviceOrder_[nIdx]));
        }
        catch( Exception e )
        {
            Log.v("DBG", "WRN: ordered idx not associated: " + nIdx + ",exception: " + e);
            return -1000.0;
        }

    }



}
