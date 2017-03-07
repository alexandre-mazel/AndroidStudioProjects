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
        Log.v("DBG", "return current activity: " + gmCurrentActivity);
        return gmCurrentActivity;
    }
    public static void setCurrentActivity(Activity currentActivity){
        Log.v( "DBG", "set current activity: " + currentActivity );
        gmCurrentActivity = currentActivity;
    }
    public static void callCurrentSensorActivityUpdate(String strDeviceName, double rAngle){
        // I should have done a super virtual class SensorsCallbackActivity with some updateAngle protected method
        Activity a = getCurrentActivity();
        if( a instanceof Menu )
        {
            ((Menu)a).updateAngle(strDeviceName,rAngle);
        }
        else
        {
            ((SettingsActivity)a).updateAngle(strDeviceName,rAngle);
        }
    }

    private static DisplaySensorActivity gmDisplayActivity = null;
    public static DisplaySensorActivity getDisplayActivity(){
        Log.v("DBG", "return current display activity: " + gmCurrentActivity);
        return getDisplayActivity;
    }
    public static void setDisplayActivity(DisplaySensorActivity currentActivity){
        Log.v( "DBG", "set current display activity: " + currentActivity );
        getDisplayActivity = currentActivity;
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
    // TODO:     private ArrayList<BluetoothGatt>            maDeviceOffset; // for each sensor, an offset, in fact, we would need a class to handle all that, even if it remains a singleton...
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

    private static AngularManager gmAngularManager = null;
    public static AngularManager getAngularManager(){
        if( gmAngularManager == null ) {
            gmAngularManager = new AngularManager(3,20);
        }
        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          Log.v("DBG", "getAngularManager return: " + gmAngularManager);
        return gmAngularManager;
    }
}
