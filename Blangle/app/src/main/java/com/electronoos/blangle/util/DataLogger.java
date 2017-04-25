package com.electronoos.blangle.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

/**
 * Created by a on 19/11/16.
 * Stores datas and output them regularily in a file
 */
public class DataLogger <T extends Number> {
    //private Queue<T> data_;
    private int nNbrValueBetweenTwoSave_ = 50;
    private int nCptValue_;
    private String strDeviceOrDataName_;
    private String strConcatenatedText_;

    public DataLogger( String strDeviceOrDataName)
    {
        strDeviceOrDataName_ = strDeviceOrDataName;
      //data_ = new ArrayDeque<T>();
        nCptValue_ = 0;
        strConcatenatedText_ = "";
    }


    public void addValue(T value)
    {
        if( true ) {
            return; // To deactivate all logers!
        }

        //data_.add(value);

        // not optimal: tout ca a chaque fois...

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strCurrentDateAndTime = sdf.format(new Date());
        String strNewLine = strCurrentDateAndTime + ": " + String.valueOf(value);
        //Log.v( "DBG", newLine );
        strConcatenatedText_ += strNewLine + "\n";
        nCptValue_ += 1;


        if( nCptValue_ > nNbrValueBetweenTwoSave_ )
        {
            // output to file
            try{
                Log.v("DBG", "DataLogger: outputting to file!!!");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, "data_" + strDeviceOrDataName_ + ".txt");

                FileOutputStream fOut = new FileOutputStream(file, true ); // true for append
                fOut.write(strConcatenatedText_.getBytes());
                fOut.close();
            }
            catch (Exception e){
                Log.v("DBG", "DataLogger: Exception: disk error: " + e.toString());
            }
            nCptValue_ = 0;
            strConcatenatedText_ = "";
        }
    }


} // class DataLogger - end
