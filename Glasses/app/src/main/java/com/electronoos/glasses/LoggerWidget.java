package com.electronoos.utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.lang.Runnable;

/**
 * Created by a on 02/05/15.
 */
public class LoggerWidget {
    private TextView textView_log_ = null;
    private int nNbrLineMax_ = 15;
    private String strCurrentLogStack_; // current log printed in the ui widget

    public LoggerWidget()
    {
        Log.v("LoggerWidget", "constructed");
    }

    public LoggerWidget( int nNbrLineMax )
    {
        nNbrLineMax_ = nNbrLineMax;
    }

    public void attachWidget( TextView tv ) {
        Log.v("LoggerWidget", "attaching a text view...");
        textView_log_ = tv;
    }

    public void refreshWidget()
    {
        if( textView_log_ != null )
            textView_log_.setText(strCurrentLogStack_);
    }

    public void l( String strCallerClassName, Object msg )
    {
        Log.v( strCallerClassName, msg.toString() );
        printToLog( "INF: " + strCallerClassName + ": " + msg.toString() );
    }

    public void w( String strCallerClassName, Object msg )
    {
        Log.w( strCallerClassName, msg.toString() );
        printToLog( "WRN: " + strCallerClassName + ": " + msg.toString() );
    }

    public void e( String strCallerClassName, Object msg )
    {
        Log.e( strCallerClassName, msg.toString() );
        printToLog( "ERR: " + strCallerClassName + ": " + msg.toString() );
    }

    private void printToLog( String msg )
    {
        if( textView_log_ == null )
        {
            //Log.e( "LoggerWidget", "Can't output log: widget not attached. Log: " + msg );
            return;
        }
        // append new log to current log outputted
        SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss");
        String timestamp = s.format(new Date());
        String strNewLog = timestamp + ": " + msg.toString();
        Log.v("debug", "GLASSES_LOG: " + strNewLog + "\n" );


        if( textView_log_ == null )
            return; // no need to print in the log!

        String strCurrentLogs = textView_log_.getText().toString();
        String lines[] = strCurrentLogs.split("\\r?\\n"); // System.getProperty("line.separator")
        ArrayList<String> listLine = new ArrayList<String>();

        int nBegin = 0;
        // copy all lines (but not the first one, sometimes)
        if( lines.length >= nNbrLineMax_ )
        {
            nBegin = 1;
        }
        for (int i = nBegin; i < lines.length; ++i)
        {
            listLine.add( lines[i] );
        }
        //listLine.add(strNew);

        String newFullLogs = TextUtils.join("\n", listLine);

        newFullLogs += "\n" + strNewLog;
        strCurrentLogStack_ = newFullLogs;


        try {
    //        textView_log_.setText(newFullLogs); // fait crasher quand pas dans le bon thread
        }
        //catch(CalledFromWrongThreadException e)
        catch(RuntimeException e)
        {
            Log.e("EXCEPTION (catched)", "GLASSES_LOG: " + e.toString() + "\n" );
        }

/*
        // runOnUiThread is an activity method, but here we're not in an activity, so bim!
        //Context context = getApplicationContext();

        runOnUiThread(new Runnable() {
            public void run(){
                refreshWidget();
            }
        });
*/

    }
/*
    protected void onPostExecute(Void result) {
        Log.l( "debug", "dans le on PostExecute du logger !!!");
    }
*/
}