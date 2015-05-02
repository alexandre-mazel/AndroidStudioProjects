package com.electronoos.utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by a on 02/05/15.
 */
public class LoggerWidget {
    private TextView textView_log_ = null;
    private int nNbrLineMax_ = 10;

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

    public void l( Object msg, String strCallerClassName )
    {
        Log.v( strCallerClassName, msg.toString() );
        printToLog( "INF: " + msg.toString(), strCallerClassName );
    }

    public void w( Object msg, String strCallerClassName )
    {
        Log.w( strCallerClassName, msg.toString() );
        printToLog( "WRN: " + msg.toString(), strCallerClassName );
    }

    public void e( Object msg, String strCallerClassName )
    {
        Log.e( strCallerClassName, msg.toString() );
        printToLog( "ERR: " + msg.toString(), strCallerClassName );
    }

    private void printToLog( String msg, String strCallerClassName )
    {
        if( textView_log_ == null )
        {
            Log.e( "LoggerWidget", "Can't output log: widget not attached. Log: " + strCallerClassName + ": " + msg );
            return;
        }
        // append new log to current log outputted
        SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss");
        String timestamp = s.format(new Date());
        String strNew = timestamp + ": " + strCallerClassName + ": " + msg.toString();


        String strCurrentLogs = textView_log_.getText().toString();
        String lines[] = strCurrentLogs.split("\\r?\\n"); // System.getProperty("line.separator")
        ArrayList<String> listLine = new ArrayList<String>();

        int nBegin = 0;
        // copy all lines (but not the first one, sometimes)
        if( lines.length > nNbrLineMax_ )
        {
            nBegin = 1;
        }
        for (int i = nBegin; i < lines.length; ++i)
        {
            listLine.add( lines[i] );
        }
        listLine.add(strNew);

        String newFullLogs = TextUtils.join("\n", listLine);


        textView_log_.setText( newFullLogs );
    }
}