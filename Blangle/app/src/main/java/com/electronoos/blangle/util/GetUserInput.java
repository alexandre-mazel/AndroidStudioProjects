package com.electronoos.blangle.util;

//import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.content.DialogInterface;


import com.electronoos.blangle.Global;

/**
 * Created by a on 13/12/16.
 */

public class GetUserInput {
    private static String gstrAnswer;
    //private static boolean gbGetAnswer;

    public static String askText() { return askText( "What is the answer?");}
    public static String askText(String strQuestion) { return askText( strQuestion, "Question" );}

    // get text input from user, return the text or null on cancel
    public static synchronized String askText( String strQuestion, String strTitle )
    {
        gstrAnswer = null;
        //gbGetAnswer = false;

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
                //notify(); // exit wait
                //gbGetAnswer = true;
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                // will return null
                //notify(); // exit wait
                //gbGetAnswer = true;
            }
        });

        alert.show();
/*
        try
        {
            alert.wait();
        }
        catch (InterruptedException e)
        {
        }
*/

        // tres crado, mais j'y arrive pas sinon
        /*
        while( ! gbGetAnswer )
        {
            try
            {
                //Thread.sleep(1000);
                Log.v("DBG", "...wait..." );
            }
            catch (InterruptedException e)
            {
            }

        }
        */
        Log.v("DBG", "askText: returning: '" + gstrAnswer + "'" );
        return gstrAnswer;

    }


} // class


   /*
String m_Input;

public synchronized String getInput()
{
    runOnUiThread(new Runnable()
    {
        @Override
        public void run()
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            //customize alert dialog to allow desired input
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    m_Input = alert.getCustomInput();
                    notify();
                }
            });
            alert.show();
        }
    });

    try
    {
        wait();
    }
    catch (InterruptedException e)
    {
    }

    return m_Input;
}
*/

/*
class AskUser2 extends AsyncTask<String, Integer, String> {
    String gstrAnswer;

    protected String doInBackground(String... strs) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Global.getCurrentActivity());
        alert.show();
        while( true ) {
            if (isCancelled()) break;
        }
        return gstrAnswer;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        //showDialog(result);
        gstrAnswer = result;
        Log.v("DBG", "askText: returning: '" + gstrAnswer + "'" );

    }
}

public class GetUserInput {
    public static void askUser2() {
        new AskUser2().execute("coucou");
    }
}
*/