package com.electronoos;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Alma on 2015-12-01.
 */

public final class WebTools {

    /**
     * Private constructor to prevent instantiation
     */
    private WebTools() {}


    // download a web file and return the contents as a string or empty string on error
    public static String getWebFile(String strRemoteAddress) {
        String strContents = "";
        int count;
        try {
            Log.e("getWebFile", "beginning");
            URL url = new URL(strRemoteAddress);
            Log.e("getWebFile", "2");
            URLConnection connection = url.openConnection();
            Log.e("getWebFile", "3");
            connection.connect();
            Log.e("getWebFile", "4");

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            //int lenghtOfFile = connection.getContentLength();
            Log.e("getWebFile", "5");

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            Log.e("getWebFile", "6");

            byte data[] = new byte[1024];

            long total = 0;

            Log.e("getWebFile", "7");

            while ((count = input.read(data)) != -1) {
                total += count;
                Log.e("getWebFile", "progression: " + total);
                Log.e("getWebFile", "8");
                strContents += data;
                Log.e("getWebFile", "9");
            }

            input.close();

        } catch (Exception e) {
            Log.e( "getWebFile", "petite erreur" );
            Log.e( "getWebFile: error: ", e.getMessage() );
        }

        return strContents;
    }
}