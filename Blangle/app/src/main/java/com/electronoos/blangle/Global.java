package com.electronoos.blangle;

import android.app.Activity;
import android.util.Log;
import java.util.Dictionary;
import java.util.Hashtable;

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

    private static Hashtable<String,Integer> gmSensorTable = null; // a way to associate a sensor string to an idx
    public static int getSensorIdx( String address ){
        if( gmSensorTable == null )
        {
            gmSensorTable = new Hashtable<String, Integer>();
        }
        if( ! gmSensorTable.containsKey(address) )
        {
            gmSensorTable.put(address, gmSensorTable.size() );
        }
        int nIdx = gmSensorTable.get(address);
        Log.v( "DBG", "getSensorIdx: " + address + " => " + nIdx );
        return nIdx;
    }

}
