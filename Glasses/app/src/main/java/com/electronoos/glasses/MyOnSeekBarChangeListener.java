package com.electronoos.glasses;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by a on 18/04/15.
 */

public class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    int nPprogress = 0;
    TextView textViewProgress;

    public void setTextViewProgress( TextView t ){
        textViewProgress = t;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        Log.v("MyActivity", "seek_age_changed: in");
        Log.v("MyActivity", "seek_age_changed: " + Integer.toString(progresValue) );
        nPprogress = progresValue;
        textViewProgress.setText(Integer.toString(progresValue));
//        Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
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
