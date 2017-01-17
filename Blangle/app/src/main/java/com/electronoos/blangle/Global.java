package com.electronoos.blangle;

import android.app.Activity;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by a on 08/10/16.
 */
public class Global {
    private static Activity gmCurrentActivity = null;
    public static Activity getCurrentActivity(){
        //Log.v("DBG", "return current activity: " + gmCurrentActivity);
        return gmCurrentActivity;
    }
    public static void setCurrentActivity(Activity currentActivity){
        Log.v( "DBG", "set current activity: " + currentActivity );
        gmCurrentActivity = currentActivity;
    }

    private static SensorManager gmCurrentSensorManager = null;
    public static SensorManager getCurrentSensorManager(){
        //Log.v("DBG", "return current SensorManager: " + gmCurrentSensorManager);
        return gmCurrentSensorManager;
    }
    public static void setCurrentSensorManager(SensorManager currentSensorManager){
        Log.v( "DBG", "set current SensorManager: " + currentSensorManager );
        gmCurrentSensorManager = currentSensorManager;
    }
}
