package com.electronoos.blangle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.electronoos.blangle.util.Averager;

/**
 * Created by a on 14/02/17.
 */
public class PasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DBG", "------------------------------");

        super.onCreate(savedInstanceState);

        Global.setCurrentActivity(this);

        setContentView(R.layout.activity_password);
    }

    @Override
    protected void onPause() {
        // we want this application to be stopped when set on background
        Log.v("DBG", "SettingsActivity: ------------------------------ onPause...");
        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.v("DBG", "SettingsActivity: ------------------------------ onStop...");
        super.onStop();
        finish();
    }



    public void onButtonGo(View view) {
        EditText te = (EditText)findViewById(R.id.password_edit);
        String strP = te.getText().toString();
        if( strP.equals( "4211") )
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return;
        }
        onBack( view );
    }


    public void onBack(View view) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }


}
