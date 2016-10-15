package com.electronoos.blangle;

import android.app.Activity;
import android.util.Log;

/**
 * Created by a on 08/10/16.
 */
public class Global {
    private static Menu gmCurrentActivity = null;
    public static Menu getCurrentActivity(){
        //Log.v("DBG", "return current activity: " + gmCurrentActivity);
        return gmCurrentActivity;
    }
    public static void setCurrentActivity(Menu currentActivity){
        Log.v( "DBG", "set current activity: " + currentActivity );
        gmCurrentActivity = currentActivity;
    }
}
