package com.electronoos.blangle.util;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by a on 19/11/16.
 * Does an Enhanced Kalman Filter on a changing data
 */
public class EKF {
    private double R_; // estimate of measurement variance, change to see effect  # 0.1**2
    private double Q_; // process variance (1e-5)

    private double xhat_;
    private double P_;

    public EKF()
    {
        R_ = 0.1*0.1; // 0.1**2
        Q_ = 5e-5;
        xhat_ = 0.;
        P_ = 1.;
    }

    public void changeProcessVariance( double Q )
    {
        Q_ = Q;
    }

    public double addValue(double value)
    {
        // return the filtered value
        // time update
        double xhatminus = xhat_;
        double Pminus = P_+Q_;

        // measurement update
        double K = Pminus/( Pminus+R_ );
        xhat_ = xhatminus+K*(value-xhatminus);
        P_ = (1-K)*Pminus;
        return xhat_;
    }

    public double getFilteredValue()
    {
        return xhat_;
    }

} // class EKF - end
