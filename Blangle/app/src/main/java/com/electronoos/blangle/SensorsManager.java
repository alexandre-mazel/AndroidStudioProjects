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
import android.util.Pair;
import android.widget.Toast;

import com.electronoos.blangle.util.GetUserInput;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Semaphore;


/**
 * Created by a on 04/10/16.
 *
 * Various documentations:
 *
 *
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/common/BleDeviceInfo.java
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/common/GattInfo.java
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/sensortag/SensorTagMovementProfile.java
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/sensortag/SensorTagGatt.java
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/sensortag/Sensor.java
 /home/a/dev/external_git/sensortag/BleSensorTag/src/main/java/com/example/ti/ble/common/BluetoothLeService.java
 /home/a/tmp2/discoveredservices.txt
 /home/a/tmp2/untitled2.txt
 /home/a/tmp2/untitled3.txt
 http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide#Movement_Sensor
 *
 *
 */
public class SensorsManager {


    private final String mstrS_HR = "0000180d-0000-1000-8000-00805f9b34fb";
    private final String mstrC_HR = "00002a37-0000-1000-8000-00805f9b34fb";
    private final String mstrS_Button = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final String mstrC_Button = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final String mstrS_Temperature = "f000aa00-0451-4000-b000-000000000000";
    private final String mstrC_Temperature = "f000aa01-0451-4000-b000-000000000000";
    private final String mstrS_Acc = "f000aa10-0451-4000-b000-000000000000";
    private final String mstrC_Acc = "f000aa11-0451-4000-b000-000000000000";
    private final String mstrS_Mov = "f000aa80-0451-4000-b000-000000000000";
    private final String mstrC_Mov = "f000aa81-0451-4000-b000-000000000000";


    private final String mstrD_Config =  "00002902-0000-1000-8000-00805f9b34fb";


    private BluetoothManager                    mManager;
    private BluetoothAdapter                    mAdapter;
    private BluetoothAdapter.LeScanCallback     mLeScanCallback;
//    private BluetoothDevice                     mDevice;
    private ArrayList<BluetoothDevice>          mListDevice;
    //private ArrayList<BluetoothGattCallback>    mleGattCallback;
    //private ArrayList<BluetoothGattCallback>    maLeGattCallback;
    private ArrayList<BluetoothGatt>            maBluetoothGatt;
    private BluetoothGattCharacteristic         mCharacToWrite;

    private long mTimeStartDiscover;
    private boolean mbScanning;
    private boolean mbConnected;
    private boolean mbNotifyAsked;
    //private boolean mbIsSensorTag; // else it's HR

    private int mnNumService;
    private int mnNumCharact;
    private int mnNumDesc;

    // sub optimal: we have a waiting list global, mais je pense qu'on pourrait en avoir une par gatt, car c'est le gatt qui bloque...
    private Queue<Pair<BluetoothGatt,Object>> mWaitingWrite;
    private Queue<Pair<BluetoothGatt,Object>> mWaitingRead;
    private Semaphore mWaitingMutex; // I want a mutex non-reentrant (even if current thread has locked it, I want to be sure, it's locked)


    public boolean isConnectedToSensorTag( BluetoothGatt gatt )
    {
        return gatt.getDevice().getName().indexOf("SensorTag")!=-1;
    }

    // do we have already found this device ?
    private boolean isAlreadyFound( BluetoothDevice newDev )
    {
        for( BluetoothDevice dev: mListDevice )
        {
            if( dev.getAddress().toString().equals( newDev.getAddress().toString() ) )
            {
                return true;
            }
        }
        return false;
    }

    public void init()
    {
        //DeviceScanActivity dsa = new DeviceScanActivity();
        //dsa.scanLeDevice( true );
        mnNumService = 0;
        mnNumCharact = 0;
        mnNumDesc = -1;

        mWaitingWrite = new ArrayDeque<Pair<BluetoothGatt,Object>>();
        mWaitingRead = new ArrayDeque<Pair<BluetoothGatt,Object>>();
        mWaitingMutex = new Semaphore(1);

        mListDevice = new ArrayList<BluetoothDevice>();
        maBluetoothGatt = new ArrayList<BluetoothGatt>();
    }
    private void addWaitingWrite(BluetoothGatt gatt, Object o)
    {
        if( o == null )
        {
            Log.v("DBG", "SensorManager: addWaitingWrite: WRN: trying to add null object!");
            return;
        }

        try
        {
            mWaitingMutex.acquire(1);
        }
        catch(Exception e)
        {
            Log.v("DBG", "ERR: SensorManager: mutex acquire unknown error! err: " + e.toString() );
        }

        try
        {
            mWaitingWrite.add( Pair.create(gatt,o) );
            if (mWaitingWrite.size() == 1) {
                _updateWaitingWrite(); // when no write are pending, let's activate the pump
            }
        }
        finally
        {
            mWaitingMutex.release(1);
        }
    }

    private void addWaitingRead(BluetoothGatt gatt, Object o)
    {
        if( o == null )
        {
            Log.v("DBG", "SensorManager: addWaitingRead: WRN: trying to add null object!");
            return;
        }

        try
        {
            mWaitingMutex.acquire(1);
        }
        catch(Exception e)
        {
            Log.v("DBG", "ERR: SensorManager: mutex acquire unknown error! err: " + e.toString() );
        }
        try {
            mWaitingRead.add( Pair.create(gatt,o) );
            if (mWaitingRead.size() == 1) {
                _updateWaitingRead(); // when no write are pending, let's activate the pump
            }
        }
        finally
        {
            mWaitingMutex.release(1);
        }
    }

    private void updateWaitingCalls()
    {

        updateWaitingWrite();
        updateWaitingRead();
    }

    private void updateWaitingWrite()
    {
        try
        {
            mWaitingMutex.acquire(1);
        }
        catch(Exception e)
        {
            Log.v("DBG", "ERR: SensorManager: mutex acquire unknown error! err: " + e.toString() );
        }
        try
        {
            _updateWaitingWrite();
        }
        finally
        {
            mWaitingMutex.release(1);
        }
    }
    private void updateWaitingRead()
    {
        try
        {
            mWaitingMutex.acquire(1);
        }
        catch(Exception e)
        {
            Log.v("DBG", "ERR: SensorManager: mutex acquire unknown error! err: " + e.toString() );
        }
        try
        {
            _updateWaitingRead();
        }
        finally
        {
            mWaitingMutex.release(1);
        }
    }

    private void _updateWaitingWrite()
    {
        if (mWaitingWrite.size() == 0)
        {
            Log.v("DBG", "SensorManager: updateWaitingWrite: WRN: empty queue?");
            return;
        }
        Pair<BluetoothGatt,Object> p = mWaitingWrite.peek();
        if( p == null ) {
            Log.v("DBG", "SensorManager: updateWaitingWrite: WRN: null object! (empty queue?)");
            return;
        }
        BluetoothGatt gatt = p.first;
        Object o = p.second;
        boolean bSuccess = false;
        if( o instanceof BluetoothGattCharacteristic )
        {
            Log.v("DBG", "SensorManager: updateWaitingWrite: writing charac");
            bSuccess = gatt.writeCharacteristic((BluetoothGattCharacteristic)o);
        }
        if( o instanceof BluetoothGattDescriptor )
        {
            Log.v("DBG", "SensorManager: updateWaitingWrite: writing desc");
            bSuccess = gatt.writeDescriptor((BluetoothGattDescriptor)o);
        }
        Log.v("DBG", "SensorManager: updateWaitingWrite: res: " + bSuccess );
        if( bSuccess )
        {
            mWaitingWrite.poll(); // remove it !
        }
        else
        {
            Log.v("DBG", "SensorManager: updateWaitingWrite: false => retrying later..." );
        }
    }

    private void _updateWaitingRead()
    {
        if (mWaitingRead.size() == 0)
        {
            Log.v("DBG", "SensorManager: updateWaitingRead: WRN: empty queue?");
            return;
        }
        Pair<BluetoothGatt,Object> p = mWaitingRead.peek();
        if( p == null ) {
            Log.v("DBG", "SensorManager: updateWaitingRead: WRN: null object! (empty queue?)");
            return;
        }
        BluetoothGatt gatt = p.first;
        Object o = p.second;
        boolean bSuccess = false;
        if( o instanceof BluetoothGattCharacteristic )
        {
            Log.v("DBG", "SensorManager: updateWaitingRead: reading charac");
            bSuccess = gatt.readCharacteristic((BluetoothGattCharacteristic)o);
        }
        if( o instanceof BluetoothGattDescriptor )
        {
            Log.v("DBG", "SensorManager: updateWaitingRead: reading desc");
            bSuccess = gatt.readDescriptor((BluetoothGattDescriptor)o);
        }
        Log.v("DBG", "SensorManager: updateWaitingRead: res: " + bSuccess );
        if( bSuccess )
        {
            mWaitingRead.poll(); // remove it !
        }
        else
        {
            Log.v("DBG", "SensorManager: updateWaitingRead: false => retrying later..." );
        }
    }

    public void discover()
    {
        Log.v("DBG", "discover");
        closeAllGatt();
        mListDevice.clear();
        mbConnected = false;
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                // your implementation here
                Log.v("DBG", "Found: Device name:" + device.getName() + ", address: " + device.getAddress() );
                if( device.getName().indexOf("SensorTag") != -1 || device.getName().indexOf("Geonaute Dual HR") != -1  )
                {
                    if( isAlreadyFound( device ) )
                    {
                        Log.v("DBG", "Already Found!");
                    }
                    else
                    {
                        Log.v("DBG", "Found an interesting one!"); // arreter l'attente ici! TODO
                        ((Menu) Global.getCurrentActivity()).updateStatus("Found");
                        //mDevice = device;
                        mListDevice.add(device);
                        //mbIsSensorTag = device.getName().indexOf("SensorTag") != -1;

                        //GetUserInput.askText("New sensor detected, name it please:");
                        //GetUserInput.askUser2();
                        //((Menu)Global.getCurrentActivity()).askUser();
                    }
                }
            }
        };
        mManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE); // storing the manager instead of an automatic make the state to disconnect just after connection!!!
        mAdapter = mManager.getAdapter();

        mAdapter.startDiscovery(); // for android 6+ Marshmallow ?

        //BluetoothManager btManager = (BluetoothManager)Global.getCurrentActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        //mAdapter = btManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mAdapter == null || !mAdapter.isEnabled()) {
            Log.v( "DBG", "bt adapter not enabled" );
            Toast.makeText(Global.getCurrentActivity(), R.string.bt_please_activate_bt, Toast.LENGTH_LONG).show();
        }


        Log.v("DBG", "start lescan, callback: " + mLeScanCallback);
        mListDevice.clear();
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
        if( mListDevice.size() < 4 && System.currentTimeMillis() - mTimeStartDiscover < 2*5000 )
            return 0;

        mbScanning = false;
        Log.v("DBG", "stop lescan");
        mAdapter.stopLeScan(mLeScanCallback);

        if( mListDevice.size() == 0 )
        {
            Log.v( "DBG", "discover: timeout!");
            ((Menu)Global.getCurrentActivity()).updateStatus( "timeout!");
            return -1;

        }
        ((Menu)Global.getCurrentActivity()).updateStatus( "found: " + mListDevice.size() );

        return 1;
    }

    private void connect()
    {
        Log.v( "DBG", "SensorManager: connecting...");
        ((Menu)Global.getCurrentActivity()).updateStatus( "connecting: " +  mListDevice.size() );
        mbConnected = true;

        for( BluetoothDevice device : mListDevice ) {

            Log.v( "DBG", "SensorManager: connecting to: " + device.getAddress() );

            BluetoothGattCallback bluetoothGattCallback =  new BluetoothGattCallback() {

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                    // this will get called anytime you perform a read or write characteristic operation
                    Log.v("DBG", "SensorManager: onCharacteristicChanged - dev: " + gatt.getDevice().getAddress() + ", time: " + String.format("%.02f", System.currentTimeMillis()/1000.) );
                    //Log.v("DBG", "SensorManager: characteristic ID: " + characteristic.getUuid().toString());
                    //read the characteristic data
                    byte[] data = characteristic.getValue();
                    //Log.v("DBG", "SensorManager: onCharacteristicChanged: " + data );
                    //logGattData(data);
                    if( characteristic.getUuid().toString().equals(mstrC_HR) ) //UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
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
                            assert( Global.getCurrentActivity() instanceof Menu );
                            ((Menu)Global.getCurrentActivity()).updateBpm( heartRate );
                            //Global.getCurrentActivity().
                        }
                        catch (Exception e) {
                            Log.v("DBG", "SensorManager: Exception: int Value?: " + e.toString() );
                        }
                    }
                    else if( characteristic.getUuid().toString().equals(mstrC_Button) )
                    {
                        int val = data[0];
                        if( (val & 1) > 0 ) {
                            Log.v("DBG", "SensorManager: onCharacteristicChanged: Button 1 (user) is pushed" );
                        }
                        if( (val & 2) > 0 ) {
                            Log.v("DBG", "SensorManager: onCharacteristicChanged: Button 2 (power) is pushed" );
                        }
                        if( val == 0 ) {
                            Log.v("DBG", "SensorManager: onCharacteristicChanged: no more button pushed" );
                        }

                    }
                    else if( characteristic.getUuid().toString().equals(mstrC_Temperature) )
                    {
                        int objectTempRaw = (data[0] & 0xff) | (data[1] << 8);
                        int ambientTempRaw = (data[2] & 0xff) | (data[3] << 8);

                        float objectTempCelsius = objectTempRaw / 128f;
                        float ambientTempCelsius = ambientTempRaw / 128f;

                        // tested: validated as in official apps
                        Log.v("DBG", "SensorManager: onCharacteristicChanged: temperature: obj: " + objectTempCelsius + ", ambi: " + ambientTempCelsius );

                    }
                    else if( characteristic.getUuid().toString().equals(mstrC_Acc) )
                    {
                        int objectTempRaw = (data[0] & 0xff) | (data[1] << 8);
                        int ambientTempRaw = (data[2] & 0xff) | (data[3] << 8);

                        float objectTempCelsius = objectTempRaw / 128f;
                        float ambientTempCelsius = ambientTempRaw / 128f;

                        Log.v("DBG", "SensorManager: onCharacteristicChanged: temperature: obj: " + objectTempCelsius + ", ambi: " + ambientTempCelsius );
                    }
                    else if( characteristic.getUuid().toString().equals(mstrC_Mov) )
                    {
                        //Log.v("DBG", "SensorManager: onCharacteristicChanged: move data received" );
                        int gyrX = (data[0] & 0xff) | (data[1] << 8);
                        int gyrY = (data[2] & 0xff) | (data[3] << 8);
                        int gyrZ = (data[4] & 0xff) | (data[5] << 8);

                        //gyrX = (gyrX * 1.0) / (65536 / 500);

                        int accX = (data[6] & 0xff) | (data[7] << 8);
                        int accY = (data[8] & 0xff) | (data[9] << 8);
                        int accZ = (data[10] & 0xff) | (data[11] << 8);

                        int magX = (data[12] & 0xff) | (data[13] << 8);
                        int magY = (data[14] & 0xff) | (data[15] << 8);
                        int magZ = (data[16] & 0xff) | (data[17] << 8);
                        //Log.v("DBG", "SensorManager: onCharacteristicChanged: move: gX: " + gyrX + ", gY: " + gyrY + ", gZ: " + gyrZ + ", aX: " + accX + ", aY: " + accY + ", aZ: " + accZ + ", mX: " + magX + ", mY: " + magY + ", mZ: " + magZ );

                        //float rAngleZ = (accZ * 1.0f) / (32768/2);
                        double rAngleZ = (accZ * 180) / 4121.; // 4200: empiric maximum // entre -4110 et 4133
                        //rAngleZ = accZ;
                        Log.v("DBG", "SensorManager: onCharacteristicChanged: move: rAngleZ: " + rAngleZ );

                        //assert( Global.getCurrentActivity() instanceof Menu );
                        //((Menu)Global.getCurrentActivity()).updateAngle( gatt.getDevice().getAddress(), rAngleZ );
                        Global.callCurrentSensorActivityUpdate( gatt.getDevice().getAddress(), rAngleZ );


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
                    Log.v("DBG", "SensorManager: onCharacteristicRead");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // temp test: continuous read!
                        onCharacteristicChanged(gatt, characteristic);
                        updateWaitingCalls();
                        //boolean resread = gatt.readCharacteristic( characteristic );
                        //Log.v("DBG", "SensorManager: resread: " + resread);
                        addWaitingRead( gatt, characteristic );
                        return;
                    }
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
                    updateWaitingCalls();
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt,
                                                  BluetoothGattDescriptor descriptor,
                                                  int status)
                {
                    Log.v("DBG", "SensorManager: onDescriptorWrite");
                    /*
                    if( mCharacToWrite != null ) {
                        boolean reswwc = gatt.writeCharacteristic(mCharacToWrite);
                        Log.v("DBG", "SensorManager: onCharacteristicWrite: write chararc: rescw: " + reswwc);
                        mCharacToWrite = null;

                    }
                    */
                    updateWaitingCalls();

                }

                @Override
                public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                    // this will get called when a device connects or disconnects
                    Log.v("DBG", "SensorManager: onConnectionStateChange: " + gatt.getDevice().getAddress() );
                    if( newState == BluetoothProfile.STATE_CONNECTED )
                    {
                        Log.v("DBG", "SensorManager: onConnectionStateChange: newState: connected");
                        Log.v("DBG", "SensorManager: onConnectionStateChange: discovering");
                        gatt.discoverServices();
                    }
                    else {
                        Log.v("DBG", "SensorManager: onConnectionStateChange: newState: " + newState);
                        ((Menu)Global.getCurrentActivity()).updateStatus("lost...");
                    }


                }

                @Override
                public void onServicesDiscovered(final BluetoothGatt gatt, final int status)
                {
                    // this will get called after the client initiates a BluetoothGatt.discoverServices() call

                    Log.v("DBG", "SensorManager: onServicesDiscovered: " + gatt.getDevice().getAddress() );

                    ((Menu)Global.getCurrentActivity()).updateStatus("discovered: " + gatt.getDevice().getAddress());

                    updateWaitingCalls();

                    if( true ) {
                        //mbDiscovered = true;

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
                            /*
                            BluetoothGatt mBG = mListDevice.get(0).connectGatt(Global.getCurrentActivity(), false, mleGattCallback);

                            BluetoothGattService service = mBG.getService(UUID.fromString(mstrS_HR));
                            // service is null here
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_HR));
                            characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                            boolean resw = mBG.writeCharacteristic(characteristic);
                            Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res2: " + resw);
                            */
                        }
                        if( ! isConnectedToSensorTag(gatt) )
                        {
                            BluetoothGattService service = gatt.getService(UUID.fromString(mstrS_HR));

                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_HR));
    /*
                            boolean rescc = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                            Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write rescc: " + rescc); // always false
                            characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                            boolean resw = mBluetoothGatt.writeCharacteristic(characteristic);
                            Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res4: " + resw); // always false
                            boolean resc = mBluetoothGatt.readCharacteristic(characteristic);
                            Log.v("DBG", "SensorManager: resc: " + resc);
    */
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                            descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    //                         descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            boolean resdes = gatt.writeDescriptor(descriptor);
                            Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: write resdes: " + resdes );
                        }

                        if( isConnectedToSensorTag(gatt) ) {
                            if( false ) {
                                // buttons
                                BluetoothGattService service = gatt.getService(UUID.fromString(mstrS_Button));
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_Button));
                                gatt.setCharacteristicNotification(characteristic, true);
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                                descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                //boolean resdes = mBluetoothGatt.writeDescriptor(descriptor);
                                //Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-desc: write resdes: " + resdes);
                                addWaitingWrite(gatt, descriptor);
                            }
                            if( false ) {
                                // humidity
                                BluetoothGattService service = gatt.getService(UUID.fromString("f000aa20-0451-4000-b000-000000000000"));
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("f000aa21-0451-4000-b000-000000000000"));
                                gatt.setCharacteristicNotification(characteristic, true);
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                addWaitingWrite(gatt, descriptor);

                                characteristic = service.getCharacteristic(UUID.fromString("f000aa22-0451-4000-b000-000000000000")); // enable
                                characteristic.setValue(new byte[]{(byte) 0x01});
                                addWaitingWrite(gatt, characteristic);
                            }
                            if( false ) {
                                // IR-temperature (temperature at pointed object) & ambi
                                BluetoothGattService service = gatt.getService(UUID.fromString(mstrS_Temperature));
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_Temperature));
                                if(false)
                                {
                                    // temps test: manual read, ici ca ne fonctionne pas, car le capteur n'est pas activé
                                    addWaitingRead(gatt, characteristic);
                                    return;
                                }
                                gatt.setCharacteristicNotification(characteristic, true);
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                addWaitingWrite(gatt, descriptor);

                                characteristic = service.getCharacteristic(UUID.fromString("f000aa02-0451-4000-b000-000000000000")); // enable
                                characteristic.setValue(new byte[]{(byte) 0x01});
                                addWaitingWrite(gatt, characteristic);
                                if(true)
                                {
                                    // temps test: manual read. ici ca fonctionne car le capteur est activé
                                    characteristic = service.getCharacteristic(UUID.fromString(mstrC_Temperature));
                                    addWaitingRead(gatt, characteristic);
                                    return;
                                }
                            }
                            if( false ) {
                                // accelero: gives inclination when static (not present in this sensortag?)
                                BluetoothGattService service = gatt.getService(UUID.fromString(mstrS_Acc));
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_Acc));
                                gatt.setCharacteristicNotification(characteristic, true);
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                addWaitingWrite(gatt, descriptor);

                                characteristic = service.getCharacteristic(UUID.fromString("f000aa12-0451-4000-b000-000000000000")); // enable
                                characteristic.setValue(new byte[]{(byte) 0x01});
                                addWaitingWrite(gatt, characteristic);
                            }
                            if( true ) {
                                // move: acc et gyro in one sensors
                                BluetoothGattService service = gatt.getService(UUID.fromString(mstrS_Mov));
                                if( service == null )
                                {
                                    Log.v("DBG", "SensorManager: onServicesDiscovered: service is null !!!" );
                                }
                                Log.v("DBG", "SensorManager: onServicesDiscovered 1: " + gatt.getDevice().getAddress() );
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_Mov));
                                gatt.setCharacteristicNotification(characteristic, true);

                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                addWaitingWrite(gatt, descriptor);
                                Log.v("DBG", "SensorManager: onServicesDiscovered 2: " + gatt.getDevice().getAddress() );

                                characteristic = service.getCharacteristic(UUID.fromString("f000aa82-0451-4000-b000-000000000000")); // One bit for each gyro and accelerometer axis (6), magnetometer (1), wake-on-motion enable (1), accelerometer range (2). Write any bit combination top enable the desired features. Writing 0x0000 powers the unit off.
                                characteristic.setValue(new byte[]{(byte) 0x7F,(byte) 0x00});
                                addWaitingWrite(gatt, characteristic);

                                if( true )
                                {
                                    // change refresh time
                                    characteristic = service.getCharacteristic(UUID.fromString("f000aa83-0451-4000-b000-000000000000"));
                                    //characteristic.setValue(new byte[]{(byte) 0x0A}); // multiple of 10ms, 10 => 100ms 0x64 => 1s
                                    characteristic.setValue(new byte[]{(byte) 0x0A}); //
                                    addWaitingWrite(gatt, characteristic);
                                }

                                if( false )
                                {
                                    // manual read
                                    characteristic = service.getCharacteristic(UUID.fromString(mstrC_Mov));
                                    addWaitingRead(gatt, characteristic);
                                }
                                Log.v("DBG", "SensorManager: onServicesDiscovered 3: " + gatt.getDevice().getAddress() );
                            }

                        }

                        if( true ) {
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
                                int prop = characteristic.getProperties();
                                Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-prop: " + prop );
                                if( prop == BluetoothGattCharacteristic.PROPERTY_NOTIFY ) Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-prop: only notify !!!" );
                                if( ( prop & BluetoothGattCharacteristic.PROPERTY_READ) > 0 ) Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-prop: has read" );
                                if( ( prop & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ) Log.v("DBG", "SensorManager: onServicesDiscovered: services-characteristic-prop: has write" );
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
                                        gatt.setCharacteristicNotification(characteristic, true);
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
            //mleGattCallback = bluetoothGattCallback;
            //maLeGattCallback.add(

            BluetoothGatt bluetoothGatt = device.connectGatt(Global.getCurrentActivity(), false, bluetoothGattCallback );
            maBluetoothGatt.add( bluetoothGatt );
            mbNotifyAsked = false;
            if (false) {
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(mstrS_HR));
                if (service == null) {
                    Log.v("DBG", "SensorManager: onServicesDiscovered: SERVICE IS NULL!");
                } else {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_HR));
                    characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                    boolean resw = bluetoothGatt.writeCharacteristic(characteristic);
                    Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res2: " + resw);
                }
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
             if( mListDevice.size() != 0 && ! mbConnected ) {
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
                 updateWaitingCalls();
/*

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
                     BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(mstrS_HR));
                     if( service == null )
                     {
                         Log.v("DBG", "SensorManager: onServicesDiscovered: service is null" );
                     }
                     else
                     {
                         BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mstrC_HR));
                         boolean rescc = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                         Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write rescc: " + rescc); // always false
                         characteristic.setValue(1, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0);
                         boolean resw = mBluetoothGatt.writeCharacteristic(characteristic);
                         Log.v("DBG", "SensorManager: onServicesDiscovered: charac heart rate write res4: " + resw); // always false
                         boolean resc = mBluetoothGatt.readCharacteristic(characteristic);
                         Log.v("DBG", "SensorManager: resc: " + resc);

                         BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(mstrD_Config));
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


                             //if( mnNumCharact < characteristics.size() ) {
                             //    boolean resb = mBluetoothGatt.readCharacteristic(characteristics.get(mnNumCharact));
                             //    Log.v("DBG", "SensorManager: resb: " + resb);
                             //}

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

*/
             }
        }
        return 1;
    }

    private void closeAllGatt()
    {
        for( BluetoothGatt gatt: maBluetoothGatt ) {
            gatt.close();
        }
        maBluetoothGatt.clear();
        mWaitingWrite.clear();
        mWaitingRead.clear();
    }

    public void exit()
    {
        Log.v("DBG", "SensorManager: exiting..." );
        mAdapter.stopLeScan(mLeScanCallback);
        closeAllGatt();
    }
}