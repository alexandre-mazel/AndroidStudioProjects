package com.electronoos.glasses;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;


import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ch.serverbox.android.usbcontroller.UsbController;
import ch.serverbox.android.usbcontroller.IUsbConnectionHandler;

import com.electronoos.utils.LoggerWidget;


public class MyActivity extends ActionBarActivity /*MyShortcuts*/ {
    public final static String EXTRA_MESSAGE = "com.electronoos.glasses.MESSAGE";

    public final static String strClassName = "MyActivity";

    private static int RESULT_LOAD_IMAGE = 1;


    private SeekBar seekBar_age_;
    public TextView textView_age_;

    private SeekBar seekBar_acidity_;
    public TextView textView_acidity_;

    public TextView textView_usb_debug_;
    public TextView textView_log_debug_;


    private static final int VID = 0x2341;
    private static final int PID = 0x0042;//I believe it is 0x0000 for the Arduino Megas // 0X0001 for uno // 0x0042 for MEgA 2560 R3
    private static UsbController usbController_;

    private static LoggerWidget logger_; // owned


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MyActivity", "onCreate: begin");
        Log.v("MyActivity", "onCreate: serial: '" + Build.SERIAL + "'");
        boolean bTablet2 = false;
        if( Build.SERIAL.equals("e3525331077a953b") )
        {
            // tablet 2
            Log.v("MyActivity", "onCreate: this is tablet 2 !!!");
            bTablet2 = true;
        }
        else
        {
            // tablet 1
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // essai pour inflater, mais ca fonctionne pas trop
        //LayoutInflater inflater = getLayoutInflater();
        //inflater.inflate(R.layout.activity_my, (ViewGroup) findViewById(R.id.container));

        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        seekBar_age_ = (SeekBar) findViewById(R.id.seek_bar_age);
        textView_age_ = (TextView) findViewById(R.id.text_view_progress_age);
        if( bTablet2 ) seekBar_age_.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_progress_2_l));



        seekBar_acidity_ = (SeekBar) findViewById(R.id.seek_bar_acidity);
        textView_acidity_ = (TextView) findViewById(R.id.text_view_progress_acidity);
        if( bTablet2 ) seekBar_acidity_.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_progress_2_r));

        //textView_usb_debug_ = (TextView) findViewById(R.id.text_view_usb_status);
        //textView_log_debug_ = (TextView) findViewById(R.id.text_view_debug_log);
        textView_usb_debug_ = null;
        textView_log_debug_ = null;

        logger_ = new LoggerWidget();
        logger_.attachWidget(textView_log_debug_);
        logger_.l( strClassName, "LOGGER BEGIN GLASSES" );

        MyOnSeekBarChangeListener myOnSeekBarChangeListener_age = new MyOnSeekBarChangeListener();
        seekBar_age_.setOnSeekBarChangeListener(myOnSeekBarChangeListener_age);
        //seekBar_age_.changeColor();

        MyOnSeekBarChangeListener myOnSeekBarChangeListener_acidity = new MyOnSeekBarChangeListener();
        seekBar_acidity_.setOnSeekBarChangeListener(myOnSeekBarChangeListener_acidity);


        if (usbController_ == null) {
            usbController_ = new UsbController(this, mConnectionHandler, VID, PID, textView_usb_debug_, logger_, this);
        }
        logger_.l( strClassName, "onCreate: usb controller: " + (usbController_ != null));
        //textView_age_.setWidth(800);
        //textView_age_.setTextSize(8);
        if( textView_usb_debug_ != null )
            textView_usb_debug_.setText(textView_usb_debug_.getText() + "\n usb : " + (usbController_ != null));

        myOnSeekBarChangeListener_age.setWidget(textView_age_, usbController_, logger_);
        myOnSeekBarChangeListener_acidity.setWidget(textView_acidity_, usbController_, logger_);

        // Various initialisation
        //((TextView) findViewById(R.id.sliders_text_desc)).setText(""); // prevent img to be hidden

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



    /*change language at Run-time*/
//use method like that:
//setLocale("en");

    public String getLocale()
    {
        // print current locale:
        String strCurrentLocal = getResources().getConfiguration().locale.toString();
        logger_.l( strClassName, "current locale: '" + strCurrentLocal + "'" );
        return strCurrentLocal;
    }

    public void setLocale(String lang) {
        logger_.l( strClassName, "Setting Locale to: '" + lang + "'" );
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        // Intent refresh = new Intent(this, AndroidLocalize.class);
        // startActivity(refresh);

        // this.recreate(); // nice but flash a bit

        // manual update:
        ((TextView) findViewById(R.id.sliders_text_title)).setText(R.string.sliders_text_title);
        // ((TextView) findViewById(R.id.sliders_text_desc)).setText(R.string.sliders_text_desc);
        //((TextView) findViewById(R.id.sliders_text_desc)).setText("");

        ((TextView) findViewById(R.id.text_view_age)).setText(R.string.text_view_age);
        ((TextView) findViewById(R.id.text_view_age_top)).setText(R.string.text_view_age_top);
        ((TextView) findViewById(R.id.text_view_age_bottom)).setText(R.string.text_view_age_bottom);

        ((TextView) findViewById(R.id.text_view_acidity)).setText(R.string.text_view_acidity);
        ((TextView) findViewById(R.id.text_view_acidity_top)).setText(R.string.text_view_acidity_top);
        ((TextView) findViewById(R.id.text_view_acidity_bottom)).setText(R.string.text_view_acidity_bottom);

    }



    /**
     * Called when the user clicks the Send button
     */
    public void refresh_usb(View view) {
        // Do something in response to button
        logger_.l( strClassName, "refresh_usb: begin");
        if( true )
        {
            if( getLocale().equals( "en" )) {
                setLocale("fr_FR");
            }
            else {
                setLocale("en");
            }
        }
        usbController_ = new UsbController(this, mConnectionHandler, VID, PID, textView_usb_debug_, logger_, this);
    }

    public void set_lang_en(View view) {
        // Do something in response to button
        logger_.l( strClassName, "set_lang_en: begin");
        setLocale("en");
    }

    public void set_lang_fr(View view) {
        // Do something in response to button
        logger_.l( strClassName, "set_lang_fr: begin");
        setLocale("fr");
    }

    public void change_aoc(View view) {
        // let user choose an image

        logger_.l( strClassName, "change_aoc: begin");

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/aocs/");
        logger_.l( strClassName, "change_aoc: uris:" + uri.toString() );
        intent.setDataAndType(uri, "image/jpg");

        startActivityForResult(intent, RESULT_LOAD_IMAGE);


/*
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                + "/aocs/");
        logger_.l( strClassName, "change_aoc: uris:" + uri.toString() );
        intent.setDataAndType(uri, "image/jpg");
        startActivity(Intent.createChooser(intent, "Open folder"));
*/
/*
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/aocs/");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "image/jpg");
        startActivity(intent);
*/
        logger_.l( strClassName, "change_aoc: end");
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
    public int getAcidity()
    {
        return seekBar_acidity_.getProgress();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String strPicturePath = cursor.getString(columnIndex);
            cursor.close();

            logger_.l( strClassName, "onActivityResult: picture_selected: " + strPicturePath );

            ImageView imageView = (ImageView) findViewById(R.id.aoc_view);
            imageView.setImageBitmap(BitmapFactory.decodeFile(strPicturePath));

            // get aoc name
            //String strAocDesc = Path(picturePath).getFileName().toString();
            String strAocDesc = strPicturePath.substring(strPicturePath.lastIndexOf("/")+1);
            strAocDesc = strAocDesc.substring(0, strAocDesc.lastIndexOf("."));
            strAocDesc = strAocDesc.replace( "_", " ");
            logger_.l( strClassName, "onActivityResult: strAocDesc: " + strAocDesc );
            ((TextView) findViewById(R.id.text_aoc_desc)).setText(strAocDesc);

            logger_.l( strClassName, "onActivityResult: end"  );

        }


    }

}
