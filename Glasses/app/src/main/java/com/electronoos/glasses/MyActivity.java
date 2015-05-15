package com.electronoos.glasses;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;


import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ch.serverbox.android.usbcontroller.UsbController;
import ch.serverbox.android.usbcontroller.IUsbConnectionHandler;

import com.electronoos.utils.LoggerWidget;


public class MyActivity extends ActionBarActivity {
    public final static String EXTRA_MESSAGE = "com.electronoos.glasses.MESSAGE";

    public final static String strClassName = "MyActivity";


    private SeekBar seekBar_age_;
    public TextView textView_age_;

    public TextView textView_usb_debug_;
    public TextView textView_log_debug_;


    private static final int VID = 0x2341;
    private static final int PID = 0x0042;//I believe it is 0x0000 for the Arduino Megas // 0X0001 for uno // 0x0042 for MEgA 2560 R3
    private static UsbController usbController_;

    private static LoggerWidget logger_; // owned


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MyActivity", "onCreate: begin");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        seekBar_age_ = (SeekBar) findViewById(R.id.seek_bar_age);
        textView_age_ = (TextView) findViewById(R.id.text_view_progress_age);
        textView_usb_debug_ = (TextView) findViewById(R.id.text_view_usb_status);
        textView_log_debug_ = (TextView) findViewById(R.id.text_view_debug_log);
        logger_ = new LoggerWidget();
        logger_.attachWidget(textView_log_debug_);
        logger_.l( strClassName, "LOGGER BEGIN GLASSES" );
        MyOnSeekBarChangeListener myOnSeekBarChangeListener = new MyOnSeekBarChangeListener();
        seekBar_age_.setOnSeekBarChangeListener(myOnSeekBarChangeListener);

        if (usbController_ == null) {
            usbController_ = new UsbController(this, mConnectionHandler, VID, PID, textView_usb_debug_, logger_, this);
        }
        logger_.l( strClassName, "onCreate: usb controller: " + (usbController_ != null));
        //textView_age_.setWidth(800);
        //textView_age_.setTextSize(8);
        textView_usb_debug_.setText(textView_usb_debug_.getText() + "\n usb : " + (usbController_ != null));

        myOnSeekBarChangeListener.setWidget(textView_age_, usbController_, logger_);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger_.l( strClassName, "onCreateOptionsMenu: begin");

        /*
        // Inflate the menu; this adds items to the action bar if it is present. (settings...)
        getMenuInflater().inflate(R.menu.menu_my, menu);
        */
        getSupportActionBar().hide(); // hide the action bar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger_.l( strClassName, "onOptionsItemSelected: begin");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendMessage(View view) {
        // Do something in response to button
        logger_.l( strClassName, "sendMessage: begin");
        /*
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */
    }


    /**
     * Called when the user clicks the Send button
     */
    public void refresh_usb(View view) {
        // Do something in response to button
        logger_.l( strClassName, "refresh_usb: begin");
        usbController_ = new UsbController(this, mConnectionHandler, VID, PID, textView_usb_debug_, logger_, this);
    }

    public void seek_age_changed(SeekBar seekBar, int progress, boolean fromUser) {
        // Do something in response to seekbar change
        // never called !!!
        logger_.l( strClassName, "seek_age_changed: in - not used!\n");
        logger_.l( strClassName, "seek_age_changed: " + Integer.toString(progress));
    }

    private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
        @Override
        public void onUsbStopped() {
            logger_.e( strClassName, "Usb stopped!");
        }

        @Override
        public void onErrorLooperRunningAlready() {
            logger_.e( strClassName, "Looper already running!");
        }

        @Override
        public void onDeviceNotFound() {
            if (usbController_ != null) {
                usbController_.stop();
                usbController_ = null;
            }
        }
    };

    public int getAge()
    {
        return seekBar_age_.getProgress();
    }

}
