package com.electronoos.glasses;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import ch.serverbox.android.usbcontroller.UsbController;

/**
 * Created by a on 18/04/15.
 */

public class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    int nPprogress = 0;
    TextView textViewProgress_;
    UsbController usbController_;

    public void setTextViewProgress( TextView t, UsbController u ){
        textViewProgress_ = t;
        usbController_ = u;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        Log.v("MyActivity", "seek_age_changed: in");
        Log.v("MyActivity", "seek_age_changed: " + Integer.toString(progresValue) );
        nPprogress = progresValue;
        textViewProgress_.setText(Integer.toString(progresValue));
//        Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();

        if(fromUser){
            if(usbController_ != null){
                usbController_.send((byte)(nPprogress&0xFF));
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
