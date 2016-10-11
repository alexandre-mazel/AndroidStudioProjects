package com.electronoos.blangle;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;


/**
 * Created by a on 04/10/16.
 */
public class SensorsManager {

    private BluetoothAdapter                    mAdapter;
    private BluetoothAdapter.LeScanCallback     mLeScanCallback;
    private BluetoothDevice                     mDevice;
    private BluetoothGattCallback               mleGattCallback;
    private BluetoothGatt                       mBluetoothGatt;

    private long mTimeStartDiscover;
    private boolean mbScanning;
    private boolean mbDiscovering;
    private boolean mbDiscovered;
    private boolean mbConnected;

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

    private int waitFound()
    {
        // return 1 if found
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

        return 1;
    }

    private void connect()
    {
        Log.v( "DBG", "SensorManager: connecting...");
        mbConnected = true;

        mbDiscovering = false;
        mleGattCallback = new BluetoothGattCallback() {

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                // this will get called anytime you perform a read or write characteristic operation
                Log.v("DBG", "SensorManager: onCharacteristicChanged");
                //read the characteristic data
                byte[] data = characteristic.getValue();
                Log.v("DBG", "SensorManager: onCharacteristicChanged: " + data );
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                // this will get called when a device connects or disconnects
                Log.v("DBG", "SensorManager: onConnectionStateChange");
                if( newState == BluetoothProfile.STATE_CONNECTED ) {
                    Log.v("DBG", "SensorManager: onConnectionStateChange: newState: connected");
                    if( ! mbDiscovering && ! mbDiscovered ) {
                        mbDiscovering = true;
                        mbDiscovered = false;
                        Log.v("DBG", "SensorManager: onConnectionStateChange: discovering");
                        gatt.discoverServices();
                    }
                }
                else
                    Log.v("DBG", "SensorManager: onConnectionStateChange: newState: " + newState );
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
                Log.v("DBG", "SensorManager: onServicesDiscovered");
                if( ! mbDiscovered ) {
                    mbDiscovered = true;
                    List<BluetoothGattService> services = gatt.getServices();
                    for (BluetoothGattService service : services) {
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        Log.v("DBG", "SensorManager: onServicesDiscovered: service-characteristics: " + characteristics);

                        for (BluetoothGattCharacteristic characteristic : characteristics )
                        {
                            //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                            //Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristics-desc from string: " + descriptor);
                            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristics-desc: " + descriptor.toString());
                                //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
                                // and then call setValue on that descriptor
                                //descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                //bluetoothGatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }
            }
        };
        mBluetoothGatt = mDevice.connectGatt(Global.getCurrentActivity(), false, mleGattCallback);
    }

    public int update()
    {
        // do whatever has to be done

        if( mbScanning ) {
            waitFound();
        }
        else
        {
         if( mDevice != null && ! mbConnected ) {
             connect();
         }
        }
        return 1;
    }
}