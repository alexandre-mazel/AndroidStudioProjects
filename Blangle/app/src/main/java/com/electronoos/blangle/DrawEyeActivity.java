package com.electronoos.blangle;

import com.electronoos.blangle.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class DrawEyeActivity extends Activity {
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

    private MyView view_;
    private int nNumFrame_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_draw_eye);

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

        view_ = new MyView(this);
        setContentView(view_);

        nNumFrame_ = 0;
        postAnimateEye(100);

    } // onCreate

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

    //@Override
    private void animateEye() {

        view_.rInterest_ *=1.001;
        if ( view_.rInterest_ > 0.9 )
        {
            view_.rInterest_ = 0.7f;
        }

        if( Math.random() > 0.9 )
        {
            if( Math.random() > 0.8 ) {
                // raz
                view_.rDstPosX_ = 0.f;
                view_.rDstPosY_ = 0.f;
            }
            else
            {
                view_.rDstPosX_ = view_.rPosX_ + ((float) Math.random() - 0.5f) * 100;
                view_.rDstPosY_ = view_.rPosY_ + ((float) Math.random() - 0.5f) * 100;
            }
        }

        float rCoef = 0.9f;
        view_.rPosX_ = view_.rPosX_ * rCoef +  view_.rDstPosX_ * (1.f-rCoef);
        view_.rPosY_ = view_.rPosY_ * rCoef +  view_.rDstPosY_ * (1.f-rCoef);

        view_.invalidate();
        postAnimateEye(50);

    }

    private void postAnimateEye(int interval)
    {
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                DrawEyeActivity.this.animateEye();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public class MyView extends View
    {
        Paint paint = null;
        float rInterest_;
        int nColor_;
        float rPosX_;
        float rPosY_;
        float rDstPosX_;
        float rDstPosY_;
        Sparkle[]  sparkle_;
        int nNbrSparkle_;
        int nRadius_;
        public MyView(Context context)
        {
            super(context);
            nRadius_ = 200;
            paint = new Paint();
            rInterest_ = 0.5f;
            nColor_ = Color.parseColor("#5CCD5C");
            rPosX_ = 0f;
            rPosY_ = 0f;
            nNbrSparkle_ = 5;
            sparkle_ = new Sparkle[nNbrSparkle_];
            for (int i = 0; i < nNbrSparkle_; ++i)
            {
                sparkle_[i] = new Sparkle( nRadius_ );
            }
        }
        public void setEye( float rInterest, int nColor, float rPosX, float rPosY )
        {
            // change eye attribut
            // rInterest: 0..1
            // rPosX: -1..1

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight();
            int radius;
            radius = nRadius_;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            int nEyeCenterX = (int) (x / 2 + rPosX_);
            int nEyeCenterY = (int) (y / 2 + rPosY_);

            paint.setColor(nColor_);
            canvas.drawCircle(nEyeCenterX, nEyeCenterY, radius, paint);

            paint.setColor(Color.parseColor("#000000"));
            canvas.drawCircle(nEyeCenterX, nEyeCenterY, radius * rInterest_, paint);
            for (int i = 0; i < nNbrSparkle_; ++i)
            {
                sparkle_[i].render(canvas, paint, nEyeCenterX, nEyeCenterY);
            }
        }
    }

    public class Sparkle
    {
        int nEyeRadius_;
        int nPosX_; // 0..100
        int nPosY_;
        int nSize_; // size when adult
        int nAge_;  // adult at 100 // disappear at xx

        public Sparkle( int nEyeRadius )
        {
            nEyeRadius_ = nEyeRadius;
            reset();
        }

        private void reset()
        {
            nPosX_ = (int)( nEyeRadius_ * 0.8 * 2 * (Math.random() - 0.5) );
            nPosY_ = (int)( nEyeRadius_ * 0.8 * 2 * (Math.random() - 0.5) );
            nSize_ = (int)( (nEyeRadius_/3) *  Math.random() + 10 );
            nAge_ = (int)(0+Math.random()*30);
        }

        private void update()
        {
            nAge_ += 10;
        }

        public void render( Canvas canvas, Paint paint, int nEyeCenterX, int nEyeCenterY )
        {
            int nAdultAgeLimit = 100;
            update();
            int nSize = (int)( nSize_ * (nAge_/(float)nAdultAgeLimit) );
            int nAlpha = 255;
            int nColor = Color.parseColor("#A0A0A0");
            if( nSize > nSize_ )
            {
                // adult
                nSize = nSize_;
                nAlpha = 255 -(nAge_ - nAdultAgeLimit)*5;
                nColor = Color.argb(nAlpha, 0xA0, 0xA0, 0xA0);
            }
            paint.setColor(nColor);
            canvas.drawCircle( nEyeCenterX+nPosX_, nEyeCenterY + nPosY_, nSize, paint);

            if( nAlpha <= 0 )
            {
                reset();
            }
        }

    }
}
