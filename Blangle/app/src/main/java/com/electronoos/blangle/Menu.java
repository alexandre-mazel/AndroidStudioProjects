package com.electronoos.blangle;

import com.electronoos.blangle.util.Averager;
import com.electronoos.blangle.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.Date;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Menu extends Activity {
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
    private TextView mTxtAngle;


    private String mstrStatus;
    private int mnBpm; // used to refresh in the good thread
    private double mrAngle; // used to refresh in the good thread
    private Averager mAngleAverage;
    private int mnNbrUpdateBpm;
    private String mstrLastTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity( this );

        setContentView(R.layout.activity_menu);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        mTxtBpm = (TextView) findViewById(R.id.menu_bpm);
        mTxtAngle = (TextView) findViewById(R.id.menu_angle);
        mTxtDeviceStatus = (TextView) findViewById(R.id.menu_txt_device_status);

        mnBpm = 0;
        mrAngle = -1.;
        mAngleAverage = new Averager<Double>(20);
        mnNbrUpdateBpm = 0;
        mstrLastTxt = "";


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
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.v("DBG", "BLE check - end");

        postConnectBLE(2000);
        postRefreshInterface( 2000 );

    } // onCreate

    @Override
    protected void onStop() {
        if( false ) {
            Log.v("DBG", "------------------------------ onStop...");
            mSensorsManager.exit();
            mSensorsManager = null;

            //super.onDestroy();
        }
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
        postConnectBLE(10);
    }

    private void connectBLE() {
        Log.v("DBG", "start BLE stuffs");
        mTxtDeviceStatus.setText("Searching...");
        if( true ) {
            mSensorsManager = new SensorsManager();
            mSensorsManager.init();
            mSensorsManager.discover();
            postRefreshBLE(1000);
        }
        if( false ) {
            Intent myIntent = new Intent(Menu.this, DeviceScanActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            Menu.this.startActivity(myIntent);
        }
        Log.v("DBG", "start BLE stuffs - end");
    }



    private void postConnectBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.connectBLE();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    private void refreshBLE() {
        Log.v("DBG", "refreshBLE");
        if( mSensorsManager != null )
            mSensorsManager.update();
        postRefreshBLE( 1000 );
        Log.v("DBG", "refreshBLE - end");
    }

    private void postRefreshBLE(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Menu.this.refreshBLE();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void updateStatus(String strStatus) {
        mstrStatus = strStatus;
    }

    public void updateBpm(int nBpm)
    {
        //Log.v("DBG", "in txtview update !!!: " + nBpm);
        //mTxtBpm.setText(String.valueOf(nBpm));
        mnBpm = nBpm; // to be refreshed later
        //postRefreshBpm( 10 ); // Can't create handler inside thread that has not called Looper.prepare()

        // output to file
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        String newLine = currentDateandTime + ": " + String.valueOf(nBpm);
        Log.v( "DBG", newLine );
        mstrLastTxt += newLine + "\n";

        mnNbrUpdateBpm += 1;
        if( mnNbrUpdateBpm > 60 ) {
            try{
                Log.v("DBG", "updateBpm: outputting to file!!!");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, "alex_hr.txt");

                FileOutputStream fOut = new FileOutputStream(file, true ); // true for append
                fOut.write(mstrLastTxt.getBytes());
                fOut.close();
            }
            catch (Exception e){
                Log.v("DBG", "updateBpm: Exception: disk error: " + e.toString());
            }
            mnNbrUpdateBpm = 0;
            mstrLastTxt = "";
        }


    }
    public void updateAngle(double rAngle)
    {
        //mTxtBpm.setText(String.valueOf(nBpm));
        //mrAngle = rAngle; // to be refreshed later
        mAngleAverage.addValue(rAngle);
        mrAngle = mAngleAverage.computeAverage().doubleValue();
    }

    private void refreshInterface() {
        //Log.v("DBG", "in refreshBpm update !!!: mnBpm:" + mnBpm);
        if( mstrStatus != null ) {
            mTxtDeviceStatus.setText(mstrStatus);
            mstrStatus = null; // could miss one from time to time
        }
        if( mnBpm != 0 ) {
            mTxtBpm.setText(String.valueOf(mnBpm));
            mnBpm = 0; // could miss one from time to time
        }
        if( mrAngle != 0. ) {
            mTxtAngle.setText( String.format("%.1f", mrAngle) + "°" );
            mrAngle = 0.; // could miss one from time to time
        }
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


}
