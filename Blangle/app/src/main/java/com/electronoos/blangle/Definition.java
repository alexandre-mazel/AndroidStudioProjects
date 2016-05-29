package com.electronoos.blangle;

import com.electronoos.blangle.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Definition extends Activity {
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

    ////////////////////////////////////////////////
    // current element
    /////////////////////////////////////

    //private CinematicElement el;
    private Printeuse mPt; // current printeuse being defined

    // interface element
    private TextView mTxtTitle;

    private LinearLayout mLayoutTextEdit1;
    private TextView mLibEdit1;
    private EditText mTxtEdit1;

    private LinearLayout mLayoutMeasureEdit1;
    private TextView mLibMeasure1;
    private EditText mMeasureEdit1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_definition);
        setupActionBar();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

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

        // Hide stuffs
        ((TextView) findViewById(R.id.fullscreen_content)).setText("");
        //((TextView) findViewById(R.id.app_name)).setText("");
        setTitle("");
        getActionBar().setIcon(R.mipmap.ic_blank);
        getActionBar().hide();

        // get interface objects
        mPt = new Printeuse();
        mTxtTitle = (TextView) findViewById(R.id.def_desc);

        mLayoutTextEdit1 = (LinearLayout) findViewById(R.id.def_layout_text_1);
        mLibEdit1 = (TextView) findViewById(R.id.def_libelle_text_1);
        mTxtEdit1 = (EditText) findViewById(R.id.def_edit_text_1);

        mLayoutMeasureEdit1 = (LinearLayout) findViewById(R.id.def_layout_measure_1);
        mLibMeasure1 = (TextView) findViewById(R.id.def_libelle_measure_1);
        mMeasureEdit1 = (EditText) findViewById(R.id.def_edit_measure_1);

        updateInterface();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void onBackToMenu(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void onValidate(View view) {
        updateInterface();
    }

    protected void updateInterface()
    {
        // update interface relatively to current printeuse state
        mPt.updateEditStep();
        int nStep = mPt.getEditStep();
        if( nStep == mPt.EDIT_STEP_NAME )
        {
            mTxtTitle.setText(R.string.def_title_printeuse_name);
            mLayoutMeasureEdit1.setVisibility(View.GONE);

            mTxtEdit1.setVisibility(View.VISIBLE);
            mLibEdit1.setText( R.string.def_libelle_printeuse_name );
            mTxtEdit1.setText("toto");
        }
        if( nStep == mPt.EDIT_STEP_REFERENCE )
        {
            mTxtTitle.setText( R.string.def_title_reference );
            mLayoutTextEdit1.setVisibility(View.GONE);

            mLayoutMeasureEdit1.setVisibility(View.VISIBLE);
            mLibMeasure1.setText( R.string.def_libelle_reference1 );

            mMeasureEdit1.setText("18");
        }
        if( nStep == mPt.EDIT_STEP_DEV_CYL )
        {
            mTxtTitle.setText( R.string.def_title_dev_cyl );
            mMeasureEdit1.setText("");
        }
        if( nStep == mPt.EDIT_STEP_LONG_RACLE )
        {
            mTxtTitle.setText( R.string.def_title_long_racle );
            mMeasureEdit1.setText("");
            mLayoutMeasureEdit1.setVisibility(View.GONE);
        }
        if( nStep == mPt.EDIT_STEP_CINEMATIC_CHOICE )
        {
            mTxtTitle.setText( R.string.def_title_cin_choice );
            mMeasureEdit1.setText("");
        }
        if( nStep == mPt.EDIT_STEP_CINEMATIC_UPPER )
        {
            mTxtTitle.setText( R.string.def_title_cin_upper );
            mMeasureEdit1.setText("");
        }
    }
}
