package com.electronoos.removeviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by a on 18/12/15.
 */

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("RemoteViewer", "StartMyServiceAtBootReceiver: on create");
    }
*/
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.v("RemoteViewer", "StartMyServiceAtBootReceiver: starting stuffs");
            Intent serviceIntent = new Intent(context, FullscreenActivity.class);
            context.startActivity(serviceIntent);
        }
    }
}
