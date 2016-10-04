package com.electronoos.blangle;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;


/**
 * Created by a on 04/10/16.
 */
public class SensorsManager {
    public void init()
    {
        DeviceScanActivity dsa = new DeviceScanActivity();
        dsa.scanLeDevice( true );
    }
}