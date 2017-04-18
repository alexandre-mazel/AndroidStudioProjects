package com.electronoos.blangle;

import com.electronoos.blangle.util.Averager;
import com.electronoos.blangle.util.GetUserInput;
import com.electronoos.blangle.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Menu extends DisplaySensorActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

//    private BluetoothAdapter mBluetoothAdapter;
    private SensorsManager   mSensorsManager;

    // interface element
    private TextView mTxtDeviceStatus;
    private TextView mTxtBpm;
    private TextView[] maTxtAngle;



    private String mstrStatus; // used to refresh in the good thread
    private int mnBpm;

    /*
    final int mnNbrAngle = 3;
    private double[] marAngle; // used to refresh in the good thread
    private double[] maTimeLastUpdateMs;
    private Averager[] maAngleAverage;
    private int mnNbrUpdateBpm;
    private String mstrLastTxt;

    */

    private TextView mTxtComputed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");
        Log.v("DBG", "Menu -- Create");
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity( this );

        setContentView(R.layout.activity_menu);
        createDisplaySensorWidgets( R.layout.activity_menu );

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

/*
        mTxtBpm = (TextView) findViewById(R.id.menu_bpm);
        maTxtAngle = new TextView[mnNbrAngle];
        maTxtAngle[0] = (TextView) findViewById(R.id.menu_angle1);
        maTxtAngle[1] = (TextView) findViewById(R.id.menu_angle2);
        maTxtAngle[2] = (TextView) findViewById(R.id.menu_angle3);
        mTxtDeviceStatus = (TextView) findViewById(R.id.menu_txt_device_status);

        mnBpm = 0;
        marAngle = new double[mnNbrAngle];
        maTimeLastUpdateMs = new double[mnNbrAngle];
        maAngleAverage = new Averager[mnNbrAngle];
        for( int i = 0; i < mnNbrAngle; ++i) {
            Log.v("DBG", "i: " + i );
            maAngleAverage[i] = new Averager<Double>(30);
        }
        mnNbrUpdateBpm = 0;
        mstrLastTxt = "";
*/
        mTxtComputed = (TextView) findViewById(R.id.menu_computed);


        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        ((TextView) findViewById(R.id.fullscreen_content)).setText("");
        //((TextView) findViewById(R.id.app_name)).setText("");
        setTitle("");
        getActionBar().setIcon(R.mipmap.ic_blank);
        getActionBar().hide();

        ////////////////////////////////////:
        /// BLE stuffs
        Log.v("DBG", "BLE check");

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.v("DBG", "NO BLE !!!");
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.v("DBG", "BLE check - end");

        //postConnectBLE(2000);
        postRefreshInterface(2000);

    } // onCreate

    @Override
    protected void onPause() {
        // we want this application to be stopped when set on background
        Log.v("DBG", "------------------------------ onPause...");
        super.onPause();
        //onStop();
        //System.exit(0); // exit this application
    }
    @Override
    protected void onStop() {
        if( true ) {
            Log.v("DBG", "------------------------------ onStop...");
            //mSensorsManager.exit();
            //mSensorsManager = null;

        }
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        return;
        /*
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.menu_radio_choice_def:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.menu_radio_choice_calc:
                if (checked)
                    // Ninjas rule
                    break;
        }
        */
    }

    public void onChoiceConnect(View view) {

        super.connect(view);
    }

    public void onChoiceSettings(View view) {
        //Intent intent = new Intent(this, SettingsActivity.class);
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
//        startActivityForResult(intent, 1);
    }

    public void onChoiceDef(View view) {
        Intent intent = new Intent(this, Definition.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void onChoiceCalc(View view) {
        // Kabloey
    }


    public void onButtonReconnect(View view) {
        super.postConnectBLE(10);
    }


    //@Override
    private void refreshInterface() {
        Log.v("DBG", "in menu refreshInterface" );
        //super.refreshDisplayInterface(false);
        /*
        //Log.v("DBG", "in refreshBpm update !!!: mnBpm:" + mnBpm);
        if( mstrStatus != null ) {
            mTxtDeviceStatus.setText(mstrStatus);
            mstrStatus = null; // could miss one from time to time
        }
        if( mnBpm != 0 ) {
            mTxtBpm.setText(String.valueOf(mnBpm));
            mnBpm = 0; // could miss one from time to time
        }
        for( int i = 0; i < mnNbrAngle; ++i) {
            if (marAngle[i] != 0.) {
                maTxtAngle[i].setText(String.format("%.1f", marAngle[i]) + "°");
                marAngle[i] = 0.; // could miss one from time to time
                if( maTimeLastUpdateMs[i] <= -1 ) {
                    maTxtAngle[i].setTextColor(Color.BLACK);
                }
                maTimeLastUpdateMs[i] = System.currentTimeMillis();
            }
            else
            {
                if( maTimeLastUpdateMs[i] > -1 )
                {
                    if( System.currentTimeMillis() - maTimeLastUpdateMs[i] > 2000 ) {
                        maTimeLastUpdateMs[i] = -1;
                        maTxtAngle[i].setTextColor(Color.RED);
                    }
                }
            }
        }
        */

        double r1 = Global.getAngularManager().getAngleByOrderedIndex(0);
        double r2 = Global.getAngularManager().getAngleByOrderedIndex(1);
        double rDiam = 30;
        double rDist = ( (r2-r1)*Math.PI*rDiam) / 360.;
        double rAngle3 = Global.getAngularManager().getAngleByOrderedIndex(2);
        mTxtComputed.setText( String.format("dist 1-2 (%.2f-%.2f, diam:%.1fcm): %.2fcm\nangle racle3: %.1f°", r1, r2, rDiam, rDist, rAngle3) );

        postRefreshInterface(500);

    }

    private void postRefreshInterface(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.refreshInterface();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void askUser() {
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                if (!isFinishing()) {
//                    new AlertDialog.Builder(Global.getCurrentActivity())
//                            .setTitle("Your Alert")
//                            .setMessage("Your Message")
//                            .setCancelable(false)
//                            .show();
//                }
//                Log.v("DBG", "askText: returning: 'debugbugu'" );
//
////                GetUserInput.askText("New sensor detected, name it please:");
//            }
//        });
        //GetUserInput.ask(this);
    }
}
