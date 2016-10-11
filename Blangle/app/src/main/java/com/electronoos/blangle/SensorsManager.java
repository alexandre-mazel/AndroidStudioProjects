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

    private BluetoothManager                    mManager;
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
        mManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        mAdapter = mManager.getAdapter();

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
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                /*
                        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                    stringBuilder.toString());
        }

                 */
            }
            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    Log.v("DBG", "SensorManager: onCharacteristicRead");
                    //read the characteristic data
                    final byte[] data = characteristic.getValue();
                    Log.v("DBG", "SensorManager: onCharacteristicRead: " + data );

                    if (data != null && data.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for(byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        Log.v("DBG", "SensorManager: onCharacteristicRead: " + new String(data) + " | " + stringBuilder.toString() );
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic,
                                        int status)
            {
                Log.v("DBG", "SensorManager: onCharacteristicWrite");
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
                // this will get called after the client initiates a BluetoothGatt.discoverServices() call

                Log.v("DBG", "SensorManager: onServicesDiscovered");
                if( ! mbDiscovered ) {
                    mbDiscovered = true;
                    List<BluetoothGattService> services = gatt.getServices();
                    for (BluetoothGattService service : services) {
                        Log.v("DBG", "SensorManager: onServicesDiscovered: ---\n" );
                        Log.v("DBG", "SensorManager: onServicesDiscovered: service: " + service);
                        Log.v("DBG", "SensorManager: onServicesDiscovered: service-uuid: " + service.getUuid());


                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        Log.v("DBG", "SensorManager: onServicesDiscovered: service-characteristics: " + characteristics);

                        for (BluetoothGattCharacteristic characteristic : characteristics )
                        {
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-uuid: " + characteristic.getUuid());
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-val: " + characteristic.getValue());
                            //Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-fval: " + characteristic.getFloatValue(0,0) );
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-prop: " + characteristic.getProperties());
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-perm: " + characteristic.getPermissions());
                            boolean res = gatt.readCharacteristic(characteristic);
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-read: res: " + res);


                            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: " + descriptor);
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc-uuid: " + descriptor.getUuid());
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc-val: " + descriptor.getValue());
                                boolean resd = gatt.readDescriptor(descriptor);
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc-read: res: " + resd);
                                if( descriptor.getUuid().toString().indexOf("00002901" ) == 0 )
                                {
                                    Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: is 901" );
                                    Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: val: " + descriptor.getValue() );
                                }

                                if( descriptor.getUuid().toString().indexOf("00002902" ) == 0 )
                                {
                                    Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: is 902" );
                                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                                    descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    boolean resw = gatt.writeDescriptor(descriptor);
                                    Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: write res: " + resw );
                                }

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
             if( mDevice != null && ! mbConnected )
             {
                 connect();
             }
             else
             {
                 // do we have something to do ?
                 Log.v("DBG", "SensorManager: updating..." );
                 //Log.v("DBG", "SensorManager: updating: state: " + mManager.getConnectionState(mDevice.getType()) );
             }
        }
        return 1;
    }
}