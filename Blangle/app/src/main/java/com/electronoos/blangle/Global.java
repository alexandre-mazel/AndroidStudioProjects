package com.electronoos.blangle;

import android.app.Activity;
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

    private static SensorsManager gmCurrentSensorsManager = null;
    public static SensorsManager getCurrentSensorsManager(){
        //Log.v("DBG", "return current SensorsManager: " + gmCurrentSensorsManager);
        return gmCurrentSensorsManager;
    }
    public static void setCurrentSensorsManager(SensorsManager currentSensorsManager){
        Log.v( "DBG", "set current SensorsManager: " + currentSensorsManager );
        gmCurrentSensorsManager = currentSensorsManager;
    }
}
