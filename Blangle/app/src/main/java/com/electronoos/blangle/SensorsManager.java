package com.electronoos.blangle;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
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
    private BluetoothGattCallback               mleGattCallback;

    private long mTimeStartDiscover;
    private boolean mbScanning;

    public void init()
    {
        //DeviceScanActivity dsa = new DeviceScanActivity();
        //dsa.scanLeDevice( true );
    }
    public void discover()
    {
        Log.v("DBG", "discover");
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
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
        mbScanning = true;
        mAdapter.startLeScan(mLeScanCallback);
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

    private int waitConnect()
    {
        // return 1 if connected
        // 0 while scan is incomplete
        // -1 if not found
        if( mDevice == null && System.currentTimeMillis() - mTimeStartDiscover < 5000 )
            return 0;

        mbScanning = false;
        Log.v("DBG", "stop lescan");
        mAdapter.stopLeScan(mLeScanCallback);

        if( mDevice == null )
        {
            Log.v( "DBG", "discover: timeout!");
            return -1;

        }
        Log.v( "DBG", "SensorManager: connecting...");

        return 1;
    }

    private void connect()
    {
        mleGattCallback = new BluetoothGattCallback() {

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                // this will get called anytime you perform a read or write characteristic operation
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                // this will get called when a device connects or disconnects
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            }
    }

    public int update()
    {
        // do whatever has to be done

        if( mbScanning ) {
            waitConnect();
        }
        else
        {
         connect();
        }
        return 1;
    }
}