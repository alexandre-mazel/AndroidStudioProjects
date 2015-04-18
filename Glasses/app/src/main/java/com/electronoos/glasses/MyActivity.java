package com.electronoos.glasses;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class MyActivity extends ActionBarActivity {
    public final static String EXTRA_MESSAGE = "com.electronoos.glasses.MESSAGE";

    private SeekBar seekBar_age_;
    public TextView textView_age_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MyActivity", "onCreate: begin");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        seekBar_age_ = (SeekBar) findViewById(R.id.seek_bar_age);
        textView_age_ = (TextView) findViewById(R.id.text_view_progress_age);
        MyOnSeekBarChangeListener myOnSeekBarChangeListener = new MyOnSeekBarChangeListener();
        seekBar_age_.setOnSeekBarChangeListener( myOnSeekBarChangeListener );
        myOnSeekBarChangeListener.setTextViewProgress( textView_age_ );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("MyActivity", "onCreateOptionsMenu: begin");

        /*
        // Inflate the menu; this adds items to the action bar if it is present. (settings...)
        getMenuInflater().inflate(R.menu.menu_my, menu);
        */
        getSupportActionBar().hide(); // hide the action bar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("MyActivity", "onOptionsItemSelected: begin");
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

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
        Log.v("MyActivity", "sendMessage: begin");
        /*
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */
    }

    public void seek_age_changed(SeekBar seekBar, int progress, boolean fromUser) {
        // Do something in response to seekbar change
        // never called !!!
        Log.v("MyActivity", "seek_age_changed: in");
        Log.v("MyActivity", "seek_age_changed: " + Integer.toString(progress) );
    }


}
