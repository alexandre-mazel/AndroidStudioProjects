package com.electronoos.hrtools;

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
    private boolean mbNotifyAsked;

    private int mnNumService;
    private int mnNumCharact;
    private int mnNumDesc;

    public void init()
    {
        //DeviceScanActivity dsa = new DeviceScanActivity();
        //dsa.scanLeDevice( true );
        mnNumService = 0;
        mnNumCharact = 0;
        mnNumDesc = -1;
    }
    public void discover()
    {
        Log.v("DBG", "discover");
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                // your implementation here
                Log.v("DBG", "Found: Device name:" + device.getName());
                if( device.getName().indexOf("SensorTag") != -1 || device.getName().indexOf("Geonaute Dual HR") != -1  )
                {
                    Log.v("DBG", "Found !"); // arreter l'attente ici! TODO
                    mDevice = device;
                }
            }
        };
        mManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE); // storing the manager instead of an automatic make the state to disconnect just after connection!!!
        mAdapter = mManager.getAdapter();
        //BluetoothManager btManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        //mAdapter = btManager.getAdapter();

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
                logGattData(data);
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
                if( true ) //UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
                {
                    int flag = characteristic.getProperties();
                    int format = -1;
                    if ((flag & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        Log.d("DBG", "Heart rate format UINT16.");
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        Log.d("DBG", "Heart rate format UINT8.");
                    }
                    try {
                        final int heartRate = characteristic.getIntValue(format, 1);
                        Log.d("DBG", String.format("Received heart rate: %d", heartRate));
                        //intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
                        ((Menu)Global.getCurrentActivity()).updateBpm( heartRate );
                        //Global.getCurrentActivity().
                    }
                    catch (Exception e) {
                        Log.v("DBG", "SensorManager: Exception: int Value?: " + e.toString() );
                    }
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
            {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    Log.v("DBG", "SensorManager: onDescriptorRead: char-uuid:" + descriptor.getUuid() );
                    //read the characteristic data
                    final byte[] data = descriptor.getValue();
                    Log.v("DBG", "SensorManager: onDescriptorRead: " + data );

                    if (data != null && data.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for(byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        Log.v("DBG", "SensorManager: onDescriptorRead: " + new String(data) + " | " + stringBuilder.toString() );
                    }
                }
            }
            private void logGattData( byte[] data )
            {
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    Log.v("DBG", "SensorManager: logGattData: " + new String(data) + " | " + stringBuilder.toString());
                }
                else
                    Log.v("DBG", "SensorManager: logGattData: null (or empty)");
            }

            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    Log.v("DBG", "SensorManager: onCharacteristicRead: char-uuid:" + characteristic.getUuid());
                    //read the characteristic data
                    final byte[] data = characteristic.getValue();
                    Log.v("DBG", "SensorManager: onCharacteristicRead: " + data);

                    //static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString( SampleGattAttributes.HEART_RATE_MEASUREMENT);

                    if( true ) //UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
                    {
                        int flag = characteristic.getProperties();
                        int format = -1;
                        if ((flag & 0x01) != 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                            Log.d("DBG", "Heart rate format UINT16.");
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                            Log.d("DBG", "Heart rate format UINT8.");
                        }
                        try {
                            final int heartRate = characteristic.getIntValue(format, 1);
                            Log.d("DBG", String.format("Received heart rate: %d", heartRate));
                            //intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
                        }
                        catch (Exception e) {
                            Log.v("DBG", "SensorManager: Exception: int Value?");
                        }
                    }
                    if( true )
                    {
                        logGattData(data);
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

                    if( false ) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            // heartService = bluetoothGatt.getService(DeviceConstants.HEART_RATE_SERVICE);
                            // heartCharact = heartService.getCharacteristic(DeviceConstants.HEART_RATE_MEASUREMENT);
                            if (service.getUuid().toString().indexOf("0000180d") == 0) {
                                // Heart Rate Service
                                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                                for (BluetoothGattCharacteristic characteristic : characteristics) {
                                    if (characteristic.getUuid().toString().indexOf("00002a37") == 0) {
                                        // Heart Rate Measurement
                                        gatt.setCharacteristicNotification(characteristic, true);
                                        //characteristic.setValue( BluetoothGattCharacteristic.PROPERTY_NOTIFY, true);
                                        characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);

//                                    descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                        boolean resw = gatt.writeCharacteristic(characteristic);
                                        Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res: " + resw);

                                    }
                                }
                            }
                        } // for services
                    }

                    if( false )
                    {
//                        BluetoothManager mBluetoothManager = mManager;
//                        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
//                        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(....);
                        BluetoothGatt mBG = mDevice.connectGatt(Global.getCurrentActivity(), false, mleGattCallback);

                        BluetoothGattService service = mBG.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
                        // service is null here
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                        characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                        boolean resw = mBG.writeCharacteristic(characteristic);
                        Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res2: " + resw);
                    }
                    if( true )
                    {
                        BluetoothGattService service = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));

                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
/*
                        boolean rescc = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                        Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write rescc: " + rescc); // always false
                        characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                        boolean resw = mBluetoothGatt.writeCharacteristic(characteristic);
                        Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res4: " + resw); // always false
                        boolean resc = mBluetoothGatt.readCharacteristic(characteristic);
                        Log.v("DBG", "SensorManager: resc: " + resc);
*/
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                         descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        boolean resdes = mBluetoothGatt.writeDescriptor(descriptor);
                        Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: write resdes: " + resdes );
                    }

                    if( false ) {
                        return;
                    }
                    // explore

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
                            logGattData( characteristic.getValue() );
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
                                    descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
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
        mbNotifyAsked = false;
        if( false ) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
            if (service == null) {
                Log.v("DBG", "SensorManager: onServicesDiscovered: SERVICE IS NULL!");
            } else {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                boolean resw = mBluetoothGatt.writeCharacteristic(characteristic);
                Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res2: " + resw);
            }
        }
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
                 try {
                     connect();
                 } catch (Exception e) {
                     Log.v("DBG", "SensorManager: EXCEPTIOPN !!!!" );
                     e.printStackTrace();
                 }
             }
             else
             {
                 // do we have something to do ?
                 Log.v("DBG", "SensorManager: updating..." );
                 //Log.v("DBG", "SensorManager: updating: state: " + mManager.getConnectionState(mDevice.getType()) );
                 //boolean resa = mBluetoothGatt.readCharacteristic( mBluetoothGatt.getServices().get(0).getCharacteristics().get(0) );
                 //resa = mBluetoothGatt.readCharacteristic( mBluetoothGatt.getServices().get(0).getCharacteristics().get(1) );
                 //Log.v("DBG", "SensorManager: resa2: " + resa); // 1 seul a la fois !!!
                 //boolean resa = mBluetoothGatt.readDescriptor( mBluetoothGatt.getServices().get(0).getCharacteristics().get(0).getDescriptors().get(0) );
                 //Log.v("DBG", "SensorManager: resa3: " + resa); // 1 seul a la fois !!!

                 // TODO: trouver le bon descriptor ou les passer tous en revue, cf le code ci dessous...
                 // avant chaque desc, on pourrait aussi afficher le service/les characteristics
                 // y a t'il
                 //BluetoothGattDescriptor desc = mBluetoothGatt.getService(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")).getDescriptor(UUID.fromString("00002908-0000-1000-8000-00805f9b34fb"));
                 //boolean resa = mBluetoothGatt.readDescriptor( desc );
                 //Log.v("DBG", "SensorManager: resa4: " + resa);

                 if( false && ! mbNotifyAsked )
                 {
                     BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
                     if( service == null )
                     {
                         Log.v("DBG", "SensorManager: onServicesDiscovered: service is null" );
                     }
                     else
                     {
                         BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                         boolean rescc = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                         Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write rescc: " + rescc); // always false
                         characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                         boolean resw = mBluetoothGatt.writeCharacteristic(characteristic);
                         Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res4: " + resw); // always false
                         boolean resc = mBluetoothGatt.readCharacteristic(characteristic);
                         Log.v("DBG", "SensorManager: resc: " + resc);

                         BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                         descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                         descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                         boolean resdes = mBluetoothGatt.writeDescriptor(descriptor);
                         Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: write resdes: " + resdes );
                         if( resdes )
                             mbNotifyAsked = true;

                     }
                 }


                 if( false ) {

                     List<BluetoothGattService> services = mBluetoothGatt.getServices();
                     if( services.size() > 0 ) {
                         List<BluetoothGattCharacteristic> characteristics = services.get(mnNumService).getCharacteristics();
                         if( mnNumCharact < characteristics.size() ) {
                             List<BluetoothGattDescriptor> descriptors = characteristics.get(mnNumCharact).getDescriptors();
                             Log.v("DBG", "SensorManager: up: " + mnNumService + " / " + services.size() + " - " + mnNumCharact + " / " + characteristics.size() + " - " + mnNumDesc + " / " + descriptors.size());

                             /*
                             if( mnNumCharact < characteristics.size() ) {
                                 boolean resb = mBluetoothGatt.readCharacteristic(characteristics.get(mnNumCharact));
                                 Log.v("DBG", "SensorManager: resb: " + resb);
                             }
                             */
                             if( mnNumDesc == -1 )
                             {
                                 // read charact
                                 boolean resc = mBluetoothGatt.readCharacteristic(characteristics.get(mnNumCharact));
                                 Log.v("DBG", "SensorManager: resc: " + resc);

                             }
                             else {
                                 if (mnNumDesc < descriptors.size()) {
                                     boolean resd = mBluetoothGatt.readDescriptor(descriptors.get(mnNumDesc));
                                     Log.v("DBG", "SensorManager: resd: " + resd);
                                 }
                             }

                             mnNumDesc += 1;
                             if (mnNumDesc >= descriptors.size()) {
                                 mnNumCharact += 1;
                                 mnNumDesc = -1;
                             }
                             if (mnNumCharact >= characteristics.size()) {
                                 mnNumService += 1;
                                 mnNumCharact = 0;
                             }

                         }
                         else
                         {
                             mnNumService += 1;
                             mnNumCharact = 0;
                         }

                         if (mnNumService >= services.size()) {
                             mnNumService = 0;
                         }
                     }
                 }
             }
        }
        return 1;
    }

    public void exit()
    {
        Log.v("DBG", "SensorManager: exiting..." );
        mAdapter.stopLeScan(mLeScanCallback);
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}