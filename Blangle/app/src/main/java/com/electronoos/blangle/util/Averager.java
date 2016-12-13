package com.electronoos.blangle.util;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by a on 19/11/16.
 * Does an average on values (filtering...)
 */
public class Averager <T extends Number> {
    private Queue<T> mData;
    int mnNbrValue;

    public Averager(int nNbrValue) {
        mData = new ArrayDeque<T>();
        mnNbrValue = nNbrValue;
    }

    public void addValue(T value)
    {
        mData.add(value);
        //Log.v("DBG", "Averager: addValue: size: " + mData.size());
        if( mData.size() > mnNbrValue )
        {
            mData.poll();
        }
    }

    public Double computeAverage() // want to put T but problem to initialise it
    {
        //Log.v("DBG", "Averager: computeAverage: size: " + mData.size());
        double sum = 0.; // new T();
        for( T t : mData )
        {
            sum = sum + t.intValue(); // explicit unboxing
        }
        return sum / mData.size(); // implicit boxing
    }

} // class Averager - end
