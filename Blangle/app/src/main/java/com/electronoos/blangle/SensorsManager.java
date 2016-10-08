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
    public void init()
    {
        //DeviceScanActivity dsa = new DeviceScanActivity();
        //dsa.scanLeDevice( true );
    }
    public void discover()
    {
        Log.v("DBG", "discover");
        BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    // your implementation here
                    Log.v("DBG", "Found: Device name:" + device.getName());
                    if( SensorTag in device.getName() )
                    {
                    }
                }
            }
        };
        BluetoothManager btManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter btAdapter = btManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.v( "DBG", "bt adapter not enabled" );
            Toast.makeText(Global.getCurrentActivity(), R.string.bt_please_activate_bt, Toast.LENGTH_LONG).show();
        }


        Log.v("DBG", "start lescan");
        btAdapter.startLeScan(leScanCallback);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        btAdapter.stopLeScan(leScanCallback);

        Log.v("DBG", "discover - end");
    }
}