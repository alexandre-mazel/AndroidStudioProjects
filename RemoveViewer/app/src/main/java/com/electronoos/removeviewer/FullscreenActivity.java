package com.electronoos.removeviewer;

import com.electronoos.removeviewer.util.SystemUiHider;
import com.electronoos.WebTools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
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

    private ArrayList aImagesToShow_;
    ReentrantLock lockImagesToShow_;
    Random random_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aImagesToShow_ = new ArrayList();
        lockImagesToShow_ = new ReentrantLock();
        random_ = new Random();

        setContentView(R.layout.activity_fullscreen);

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

        Log.e( "RemoteViewer: onCreate", "before downloading...");
        //new DownloadFileFromURL().execute("http://perso.ovh.net/~mangedisf/mangedisque/images/bg_klee.gif");
        /*
        DownloadFileFromURL dffu = new DownloadFileFromURL();
        dffu.setParent( this );
        //String strSrc ="http://perso.ovh.net/~mangedisf/mangedisque/images/bg_klee.gif";
        String strSrc = "http://perso.ovh.net/~mangedisf/mangedisque/logo_test/logo_cdl_white.png";
        dffu.execute( strSrc );
        */

        //new Timer().schedule(this.showRandomImage(), 1000);

        /*
        //new Timer().schedule({this.updateDirectory()}, 1);
        int interval = 1000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Toast.makeText(FullscreenActivity.this, "upgrading stuffs", Toast.LENGTH_SHORT).show();
                FullscreenActivity.this.updateDirectory();
            }
        };

        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
        */

        webUpdate();
        postRedraw();
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

    @Override
    protected void onPause() {
        // we want this application to be stopped when set on background
        super.onPause();
        System.exit(0); // exit this application
    }

    

    public void showImage( String strPicturePath )
    {
        Log.v( "Viewer", "onActivityResult: picture_selected: " + strPicturePath );

        ImageView imageView = (ImageView) findViewById(R.id.aoc_view);
        imageView.setImageBitmap(BitmapFactory.decodeFile(strPicturePath));
    }

    public void updateDirectory()
    {
        /*
        Toast.makeText(FullscreenActivity.this, "updating directory", Toast.LENGTH_SHORT).show();
        //String strRemotePath = "http://candilinge.factorycity.com/img_ochateau";
        String strRemotePath = "http://perso.ovh.net/~mangedisf/mangedisque/logo_test/logo_cdl_white.png";
        Log.v( "RemoteViewer", "updateDirectory: " + strRemotePath );
        String strIndex = WebTools.getWebFile(strRemotePath);
        Log.v( "RemoteViewer", "strIndex: " + strIndex );
        */
    }

    public void setImagesToShow(ArrayList listImg)
    {
        Log.v( "RemoteViewer", "setImagesToShow: in" );
        lockImagesToShow_.lock();
        aImagesToShow_ = listImg;
        lockImagesToShow_.unlock();
    }

    private void showRandomImage()
    {
        Log.v( "RemoteViewer", "showRandomImage: in" );
        lockImagesToShow_.lock();
        if( aImagesToShow_.size() > 0 )
        {
            int nIdx = random_.nextInt(aImagesToShow_.size());
            //nIdx = 0; // force first (debug)
            String img = aImagesToShow_.get(nIdx).toString();
            Log.v( "RemoteViewer", "showRandomImage: showing: " + img );
            Toast.makeText(FullscreenActivity.this, "redrawing: " + img, Toast.LENGTH_SHORT).show();
            showImage(img);
        }
        lockImagesToShow_.unlock();
        postRedraw();
    }

    private void postRedraw()
    {
        if( false )
            return;
        int interval = 5000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                FullscreenActivity.this.showRandomImage();
            }
        };

        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    private void webUpdate()
    {
        Toast.makeText(this, "updating folders...", Toast.LENGTH_SHORT).show();
        DownloadDirectoryFromURL ddfu = new DownloadDirectoryFromURL();
        ddfu.setParent( this );
        //String strSrc = "http://candilinge.factorycity.com/img/";
        String strSrc = "http://perso.ovh.net/~mangedisf/mangedisque/logo_test/";
        ddfu.execute( strSrc );

        int nTimeRefresh = 1000*60*60*8; // 8h
        //nTimeRefresh = 1000 * 10; // test refresh
        if( true ) {
            // compute time until 2AM
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();

            nTimeRefresh = ((24-today.hour) + 2) * (60*60*1000) - (today.minute*1000*60);
        }
        postWebUpdate( nTimeRefresh );
    }
    private void postWebUpdate(int interval)
    {
        Log.v( "RemoteViewer", "postWebUpdate: update in: " + interval );
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Toast.makeText(FullscreenActivity.this, "redrawing", Toast.LENGTH_SHORT).show();
                FullscreenActivity.this.webUpdate();
            }
        };

        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }
}



/**
 * Background Async Task to download file
 * */
class DownloadFileFromURL extends AsyncTask<String, String, String> {

    private FullscreenActivity parentActivity_; // parent that launch this request
    private String strImage_; // destination filename

    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // showDialog(progress_bar_type);
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            Log.e( "RemoteViewer: doInBackground", "beginning");
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            strImage_ = Environment.getExternalStorageDirectory().toString() + "/test.jpg";
            OutputStream output = new FileOutputStream(strImage_);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("RemoteViewer: error: while downloading: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        //pDialog.setProgress(Integer.parseInt(progress[0]));
        Log.e( "RemoteViewer: ", progress[0]);
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
        Log.e( "RemoteViewer: ", "finito!");
        parentActivity_.showImage(strImage_);

    }

    public void setParent( FullscreenActivity activity )
    {
        parentActivity_ = activity;
    }
}


/**
 * Background Async Task to download file
 * */
class DownloadDirectoryFromURL extends AsyncTask<String, String, String> {

    private FullscreenActivity parentActivity_; // parent that launch this request
    private ArrayList listFiles_; // list of destination filename

    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // showDialog(progress_bar_type);
    }

    /**
     * Downloading a full directory in background thread
     *
     * f_url: directory path
     *

     * */
    @Override
    protected String doInBackground(String... f_url) {
        listFiles_ = new ArrayList();
        //String strRemotePath = "http://candilinge.factorycity.com/img_ochateau";
        String strRemotePath = f_url[0];
        Log.v( "RemoteViewer", "updateDirectory: " + strRemotePath );
        String strIndex = WebTools.getWebFile(strRemotePath);
        Log.v( "RemoteViewer", "strIndex: " + strIndex );
        String[] listFile = WebTools.findFilesInIndexes( strIndex );
        Log.v( "RemoteViewer", "listFile: " + listFile.toString() );

        for (String strFile: listFile){
            Log.v( "RemoteViewer", "loading: " + strFile );
            String strDest = Environment.getExternalStorageDirectory().toString() + "/" + strFile;
            WebTools.saveWebFile(strRemotePath+strFile, strDest );
            listFiles_.add( strDest );
        }

        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        //pDialog.setProgress(Integer.parseInt(progress[0]));
        Log.e( "RemoteViewer: ", progress[0]);
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
        Log.e( "RemoteViewer: ", "finito!");
        parentActivity_.setImagesToShow(listFiles_);

    }

    public void setParent( FullscreenActivity activity )
    {
        parentActivity_ = activity;
    }
}

