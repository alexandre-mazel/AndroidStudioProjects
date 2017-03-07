package com.electronoos.blangle;

import android.util.Log;

import com.electronoos.blangle.util.Averager;

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
    }

    public void calibrate( int nIdx )
    {
        Log.d( "DBG", "AngularManager.calibrate: " + nIdx + ", old: " + arOffset_[nIdx] + " => " + arAngle_[nIdx] );
        arOffset_[nIdx] = arAngle_[nIdx];
    }


}
