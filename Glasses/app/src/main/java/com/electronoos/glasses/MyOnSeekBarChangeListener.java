package com.electronoos.glasses;

import android.os.SystemClock;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import ch.serverbox.android.usbcontroller.UsbController;
import com.electronoos.utils.LoggerWidget;

/**
 * Created by a on 18/04/15.
 */

public class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    int nPprogress = 0;
    TextView textViewProgress_;
    UsbController usbController_;
    LoggerWidget logger_;
    private final static String strClassName = "MyOnSeekBarChangeListener";

    public void setWidget( TextView t, UsbController u, LoggerWidget logger ){
        textViewProgress_ = t;
        usbController_ = u;
        logger_ = logger;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {

        logger_.l( strClassName, "seek_changed: in");
        logger_.l( strClassName, "seek_changed: " + Integer.toString(progresValue) );
        logger_.l( strClassName, "from user: " + Boolean.toString(fromUser) );
        nPprogress = progresValue;
        textViewProgress_.setText(Integer.toString(progresValue));
//        Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();

        if(fromUser || true ){
            if(usbController_ != null){
                logger_.l( strClassName, "sending!" );
                usbController_.send((byte)((nPprogress)&0xFF)); // the value is not used!
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //textView.setText("Covered: " + progress + "/" + seekBar.getMax());
        //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
    }
}
