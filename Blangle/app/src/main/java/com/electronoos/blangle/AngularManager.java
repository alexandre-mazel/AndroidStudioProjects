package com.electronoos.blangle;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.electronoos.blangle.util.Averager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Handle all stuffs needed to manage a bunch of sensors (mainly angular one, using BTLE)
 * Created by a on 19/02/17.
 */
public class AngularManager {

    private int nNbrSensors_;

    // for each sensors:
    private Averager[] aAngleAverager_;
    private double[] arAngle_; // Last measured/averaged angle

    private double[] arOffset_;

    private double[] aTimeLastUpdateMs_;

    private Hashtable<String,Integer> sensorTable_; // a way to associate a sensor string to an idx (idx is then the index of above list)

    File pathForConfig_;


    // nNbrValueToAverage => in BTLE each second gives 10 values... so 20 to 30 is ok
    public AngularManager( int nNbrSensors, int nNbrValueToAverage )
    {
        nNbrSensors_ = nNbrSensors;
        aAngleAverager_ = new Averager[nNbrSensors_];
        arAngle_ = new double[nNbrSensors_];
        arOffset_= new double[nNbrSensors_];
        aTimeLastUpdateMs_ = new double[nNbrSensors_];

        for (int i = 0; i < nNbrSensors_; ++i) {
            aAngleAverager_[i] = new Averager<Double>(nNbrValueToAverage);
            arAngle_[i] = -1;
            arOffset_[i] = 0;

        }
        sensorTable_ = new Hashtable<String, Integer>();

        //File pathForConfig_ = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File pathForConfig_ = Context.getCacheDir() + File.separator;
        File pathForConfig_ = Environment.getDataDirectory();

        readConfig();
    }

    private void readConfig()
    {
        Log.v("DBG", "readConfig: reading from file!!!");
        try
        {
            //File file = new File( pathForConfig_, "blangle.cfg");

            //FileInputStream fIn = new FileInputStream(file );
            FileInputStream fIn = Global.getDisplayActivity().getApplicationContext().openFileInput("blangle.cfg"); //Context.MODE_PRIVATE
            DataInputStream dis = new DataInputStream( fIn );
            int n = dis.readInt();
            Log.v("DBG", "readConfig: " + n );
            for( int i = 0; i < n; ++i )
            {
                String s = dis.readUTF();
                Double d = dis.readDouble();
                Log.v("DBG", "readConfig: " + s + ": " + d );
                arOffset_[sensorTable_.size()] = d;
                sensorTable_.put(s, sensorTable_.size() );
            }

            dis.close();
            Log.v("DBG", "readConfig: read succeeded...");
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
            FileOutputStream fOut = Global.getDisplayActivity().getApplicationContext().openFileOutput("blangle.cfg",Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream( fOut );
            dos.writeInt( getDetectedSensorNbr() );
            for (String key : sensorTable_.keySet())
            {
                dos.writeUTF( key );
                dos.writeDouble( arOffset_[sensorTable_.get(key)] );
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
        if( ! sensorTable_.containsKey(address) )
        {
            sensorTable_.put(address, sensorTable_.size() );
        }
        int nIdx = sensorTable_.get(address);
        //Log.v( "DBG", "getSensorIdx: " + address + " => " + nIdx );
        return nIdx;
    }

    public void updateAngle(String strDeviceName, double rAngle)
    {
        int nCurrentIdx = getSensorIdx(strDeviceName);
        aAngleAverager_[nCurrentIdx].addValue(rAngle);
        arAngle_[nCurrentIdx] = aAngleAverager_[nCurrentIdx].computeAverage().doubleValue();
        aTimeLastUpdateMs_[nCurrentIdx] = System.currentTimeMillis();
    }

    public double getAngle( int nIdx )
    {
        return arAngle_[nIdx] - arOffset_[nIdx];
    }
    public double getLastUpdate( int nIdx )
    {
        return aTimeLastUpdateMs_[nIdx];
    }

    // return the number of detected sensors
    public int getDetectedSensorNbr(){
        return sensorTable_.size();
    }

    public void calibrateAll()
    {
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


}
