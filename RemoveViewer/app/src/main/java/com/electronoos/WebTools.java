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

    public static String[] findFilesInIndexes( String strPage ){
        // takes an html index pages and return a list of file present in the index
        // return ["cerveau.gif", "chef.jpg"]

        String[] listFiles = new String[34];
        Integer nNbrFile = 0;
        String strRemaining = strPage;
        while( true ) {
            Integer nIdx = strRemaining.indexOf("a href=\"");
            if( nIdx < 0 )
                break;
            strRemaining=strRemaining.substring(nIdx);
            Integer nEnd = strRemaining.indexOf("\">");
            String strFilename = strRemaining.substring( 0+8, nEnd ); // 7 is the size of "a href=""
            if( strFilename.charAt(0) != '?' && strFilename.charAt(0) != '/'  ) {
                listFiles[nNbrFile] = strFilename;
                nNbrFile += 1;
            }
            strRemaining=strRemaining.substring(nEnd);
        }

        return listFiles;
    }




    // download a web file and return the contents as a string or empty string on error
    public static String getWebFile(String strRemoteAddress) {
        String strContents = "";
        int count;
        try {
            Log.e("getWebFile", "beginning");
            URL url = new URL(strRemoteAddress);
            Log.e("getWebFile", "2");
            URLConnection connection = url.openConnection(); // Wrning: ne pas faire un "Network on main thread" exception !
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
                String converted = new String(data, "UTF-8");
                strContents += converted;
                Log.e("getWebFile", "9");
            }

            input.close();

        } catch (Exception e) {
            Log.e( "getWebFile", "petite erreur" );
            Log.e( "getWebFile: error: ", "error: " + (String)e.getMessage() + ", cause: " + e.toString() );
        }

        Log.e("getWebFile", "end");
        return strContents;
    }
}