package com.electronoos.blangle;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by a on 04/10/16.
 */
public class SensorsManager {

    private BluetoothAdapter                    mAdapter;
    private BluetoothAdapter.LeScanCallback     mLeScanCallback;
    private BluetoothDevice                     mDevice;

    private long mTimeStartDiscover;

    public void init()
    {
        //DeviceScanActivity dsa = new DeviceScanActivity();
        //dsa.scanLeDevice( true );
    }
    public void discover()
    {
        Log.v("DBG", "discover");
        BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                // your implementation here
                Log.v("DBG", "Found: Device name:" + device.getName());
                if( device.getName().indexOf("SensorTag") != -1 )
                {
                    Log.v("DBG", "Found !"); // arreter l'attente ici! TODO
                    mDevice = device;
                }
            }
        };
        BluetoothManager btManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        mAdapter = btManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mAdapter == null || !mAdapter.isEnabled()) {
            Log.v( "DBG", "bt adapter not enabled" );
            Toast.makeText(Global.getCurrentActivity(), R.string.bt_please_activate_bt, Toast.LENGTH_LONG).show();
        }


        Log.v("DBG", "start lescan");
        mDevice = null;
        mTimeStartDiscover = System.currentTimeMillis();
        mAdapter.startLeScan(leScanCallback);
        /*
        try {
            for(int l=0; l<=10; l++){
                Log.v( "DBG", "loop...");
                Thread.sleep(1000);
                if( mDevice != null )
                {
                    break;
                }
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

        Log.v("DBG", "discover - end (running in background)");
    }

    public int connect()
    {
        // return 1 if connected
        // 0 while scan is incomplete
        // -1 if not found
        if( mDevice != null && System.currentTimeMillis() - mTimeStartDiscover < 5000 )
            return 0;

        mAdapter.stopLeScan(mLeScanCallback);

        if( mDevice == null )
        {
            Log.v( "DBG", "discover: timeout!");
            return -1;

        }
        Log.v( "DBG", "SensorManager: connecting...");

    }
}