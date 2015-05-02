package com.electronoos.utils;

import android.text.TextUtils;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by a on 02/05/15.
 */
public class Debug
{
    private final TextView textView_log_;
    private final int nNbrLineMax_;

    public Debug( int nNbrLineMax = 10 ) {

    }

    public setLogTextView( TextView tv ) {
        textView_log_ = tv;
    }

    public l( msg, String strCallerClass = "?" )
    {
        Log.l( strCallerClass, msg );
        printToLog( "INF: " + msg, strCallerClass );
    }

    public w( msg, String strCallerClass = "?" )
    {
        Log.w( strCallerClass, msg );
        printToLog( "WRN: " + msg, strCallerClass );
    }

    public e( msg, String strCallerClass = "?" )
    {
        Log.e( strCallerClass, msg );
        printToLog( "ERR: " + msg, strCallerClass );
    }

    private void printToLog( String msg, String strCallerClass = "?" )
    {
        // append new log to current log outputted
        SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss");
        String timestamp = s.format(new Date());
        String strNew = timestamp + ": " + strCallerClass + ": " + msg.toString();


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

}
