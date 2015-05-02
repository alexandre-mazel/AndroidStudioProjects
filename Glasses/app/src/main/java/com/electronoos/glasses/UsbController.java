/*
 * UsbController.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.serverbox.android.usbcontroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

/**
 * (c) Neuxs-Computing GmbH Switzerland
 * @author Manuel Di Cerbo, 02.02.2012
 *
 */
public class UsbController {

	private final Context mApplicationContext;
	private final UsbManager mUsbManager;
    private final TextView textView_usb_debug_; // not owned
    private final TextView textView_debug_log_; // not owned
	private final ch.serverbox.android.usbcontroller.IUsbConnectionHandler mConnectionHandler;
	private final int VID;
	private final int PID;
	protected static final String ACTION_USB_PERMISSION = "ch.serverbox.android.USB";

	/**
	 * Activity is needed for onResult
	 * 
	 * @param parentActivity
	 */
	public UsbController(Activity parentActivity,
			IUsbConnectionHandler connectionHandler, int vid, int pid, TextView textView_usb_debug,TextView textView_debug_log) {
		mApplicationContext = parentActivity.getApplicationContext();
		mConnectionHandler = connectionHandler;
		mUsbManager = (UsbManager) mApplicationContext
				.getSystemService(Context.USB_SERVICE);
		VID = vid;
		PID = pid;
        textView_usb_debug_ = textView_usb_debug;
        textView_debug_log_ = textView_debug_log;
		init();
	}

	private void init() {
		enumerate(new IPermissionListener() {
			@Override
			public void onPermissionDenied(UsbDevice d) {
				UsbManager usbman = (UsbManager) mApplicationContext
						.getSystemService(Context.USB_SERVICE);
				PendingIntent pi = PendingIntent.getBroadcast(
						mApplicationContext, 0, new Intent(
								ACTION_USB_PERMISSION), 0);
				mApplicationContext.registerReceiver(mPermissionReceiver,
						new IntentFilter(ACTION_USB_PERMISSION));
				usbman.requestPermission(d, pi);
			}
		});
	}

	public void stop() {
		mStop = true;
		synchronized (sSendLock) {
			sSendLock.notify();
		}
		try {
			if(mUsbThread != null)
				mUsbThread.join();
		} catch (InterruptedException e) {
			e(e);
		}
		mStop = false;
		mLoop = null;
		mUsbThread = null;
		
		try{
			mApplicationContext.unregisterReceiver(mPermissionReceiver);
		}catch(IllegalArgumentException e){};//bravo
	}

	private UsbRunnable mLoop;
	private Thread mUsbThread;

	private void startHandler(UsbDevice d)
    {
		if (mLoop != null)
        {
            e("lopper running already");
			mConnectionHandler.onErrorLooperRunningAlready();
			return;
		}
		mLoop = new UsbRunnable(d);
		mUsbThread = new Thread(mLoop);
		mUsbThread.start();
        l("stread started");
	}

	public void send(byte data) {
		mData = data;
        textView_usb_debug_.setText( "sending: " + Integer.toString(data) );
		synchronized (sSendLock) {
			sSendLock.notify();
		}
	}

	private void enumerate(IPermissionListener listener) {
		l("enumerating");
        textView_usb_debug_.setText( "enumerating..." );
		HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
        String infoDeviceLog = "usb devices: ";
		while (deviter.hasNext()) {
			UsbDevice d = deviter.next();
            String infoDevice = "Found device: " + String.format("%04X:%04X", d.getVendorId(),d.getProductId());
			l( infoDevice );
            infoDeviceLog += infoDevice + "\n";
			if (d.getVendorId() == VID && d.getProductId() == PID) {
				l("Device under: " + d.getDeviceName());
                infoDeviceLog += "recognized...\n";
                //SystemClock.sleep(1000);
				if (!mUsbManager.hasPermission(d))
                {
					listener.onPermissionDenied(d);
                    infoDeviceLog += "permission denied...\n";
                    l("permission denied\n");
                }
				else
                {
                    infoDeviceLog += "selected...\n";
					startHandler(d);
					return;
				}
				break;
			}
		}
        infoDeviceLog += "ended...\n";
        textView_usb_debug_.setText( infoDeviceLog );
		l("no more devices found");
		mConnectionHandler.onDeviceNotFound();
	}

	private class PermissionReceiver extends BroadcastReceiver {
		private final IPermissionListener mPermissionListener;

		public PermissionReceiver(IPermissionListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
            l("onReceive: enter");
			mApplicationContext.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false))
                {
                    l("onReceive: perm denied");
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					l("onReceive: Permission granted");
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (dev.getVendorId() == VID
								&& dev.getProductId() == PID)
                        {
                            l("onReceive: start new thread");
                            startHandler(dev);// has new thread
						}
					} else {
						e("device not present!");
					}
				}
			}
		}

	}

	// MAIN LOOP
	private static final Object[] sSendLock = new Object[]{};//learned this trick from some google example :)
	//basically an empty array is lighter than an  actual new Object()...
	private boolean mStop = false;
	private byte mData = 0x00;

	private class UsbRunnable implements Runnable {
		private final UsbDevice mDevice;
	
		UsbRunnable(UsbDevice dev) {
			mDevice = dev;
		}
	
		@Override
		public void run() {//here the main USB functionality is implemented
			UsbDeviceConnection conn = mUsbManager.openDevice(mDevice);
			if (!conn.claimInterface(mDevice.getInterface(1), true)) {
				return;
			}
			// Arduino Serial usb Conv
			conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
			conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
					0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
	
			UsbEndpoint epIN = null;
			UsbEndpoint epOUT = null;
	
			UsbInterface usbIf = mDevice.getInterface(1);
			for (int i = 0; i < usbIf.getEndpointCount(); i++) {
				if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
						epIN = usbIf.getEndpoint(i);
					else
						epOUT = usbIf.getEndpoint(i);
				}
			}
	
			for (;;) {// this is the main loop for transferring
				synchronized (sSendLock) {//ok there should be a OUT queue, no guarantee that the byte is sent actually
					try {
						sSendLock.wait();
					} catch (InterruptedException e) {
						if (mStop) {
							mConnectionHandler.onUsbStopped();
							return;
						}
						e.printStackTrace();
					}
				}
				conn.bulkTransfer(epOUT, new byte[] { mData }, 1, 0);
	
				if (mStop) {
					mConnectionHandler.onUsbStopped();
					return;
				}
			}
		}
	}

	// END MAIN LOOP
	private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
			new IPermissionListener() {
				@Override
				public void onPermissionDenied(UsbDevice d) {
					l("Permission denied on " + d.getDeviceId());
				}
			});

	private static interface IPermissionListener {
		void onPermissionDenied(UsbDevice d);
	}

	public final static String TAG = "USBController";

	private void l(Object msg)
    {
		Log.d(TAG, ">==< " + msg.toString() + " >==<");
        printToLog( TAG + ": " + msg );
	}

	private void e(Object msg)
    {
		Log.e(TAG, ">==< " + msg.toString() + " >==<");
        printToLog( TAG + ": ERR: " + msg.toString());
	}

    private void printToLog( Object msg )
    {
        SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss");
        String timestamp = s.format(new Date());
        String strNew = timestamp + ": " + TAG + ": " + msg.toString();


        String strCurrentLogs = textView_debug_log_.getText().toString();
        String lines[] = strCurrentLogs.split("\\r?\\n"); // System.getProperty("line.separator")
        ArrayList<String> listLine = new ArrayList<String>();

        int nBegin = 0;
        // copy all lines (but not the first one, sometimes)
        if( lines.length > 10 )
        {
            nBegin = 1;
        }
        for (int i = nBegin; i < lines.length; ++i)
        {
            listLine.add( lines[i] );
        }
        listLine.add(strNew);

        String newFullLogs = TextUtils.join("\n", listLine);


        textView_debug_log_.setText( newFullLogs );
    }
}