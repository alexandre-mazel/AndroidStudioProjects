package com.electronoos.blangle;

import android.util.Log;

import com.electronoos.blangle.util.Averager;

/**
 * Created by a on 19/02/17.
 */
public class AngularManager {

    private int nNbrSensors_;

    // for each sensors:
    private Averager[] aAngleAverager_;
    private double[] arAngle_; // Last mesured/averaged angle

    // nNbrValueToAverage => in BTLE each second gives 10 values... so 20 to 30 is ok
    public AngularManager( int nNbrSensors, int nNbrValueToAverage )
    {
        nNbrSensors_ = nNbrSensors;
        arAngle_ = new double[nNbrSensors_];
        aAngleAverager_ = new Averager[nNbrSensors_];
        for (int i = 0; i < nNbrSensors_; ++i) {
            aAngleAverager_[i] = new Averager<Double>(nNbrValueToAverage);
        }
    }

    public void updateAngle(String strDeviceName, double rAngle)
    {
        int nCurrentIdx = Global.getSensorIdx(strDeviceName);
        aAngleAverager_[nCurrentIdx].addValue(rAngle);
        arAngle_[nCurrentIdx] = aAngleAverager_[nCurrentIdx].computeAverage().doubleValue();
    }


}
