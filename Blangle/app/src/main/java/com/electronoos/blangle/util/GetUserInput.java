package com.electronoos.blangle.util;

//import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.content.DialogInterface;


import com.electronoos.blangle.Global;

/**
 * Created by a on 13/12/16.
 */

public class GetUserInput {
    private static String gstrAnswer;

    public static String askText() { return askText( "What is the answer?");}
    public static String askText(String strQuestion) { return askText( strQuestion, "Question" );}

    // get text input from user, return the text or null on cancel
    public static synchronized String askText( String strQuestion, String strTitle )
    {
        gstrAnswer = null;

        AlertDialog.Builder alert = new AlertDialog.Builder(Global.getCurrentActivity());

        alert.setTitle(strTitle);
        alert.setMessage(strQuestion);

// Set an EditText view to get user input
        final EditText input = new EditText(Global.getCurrentActivity());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                gstrAnswer = value;
                notify(); // exit wait
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                // will return null
                notify(); // exit wait
            }
        });

        alert.show();
        try
        {
            alert.wait();
        }
        catch (InterruptedException e)
        {
        }

        Log.v("DBG", "askText: returning: '" + gstrAnswer + "'" );
        return gstrAnswer;

    }


}
