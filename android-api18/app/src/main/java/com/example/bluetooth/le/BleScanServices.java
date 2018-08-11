package com.example.bluetooth.le;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Owner on 7/31/2018.
 */

public class BleScanServices {


    public static BluetoothAdapter bleAdapter;
    public static boolean bleScanner;
    public static Handler bleHandler = new Handler();
    public static final long BLE_SCAN_PERIOD = 2000;
    public static final int REQUEST_ENABLE_BT = 1;
    public BluetoothAdapter.LeScanCallback mLeScanCallback;


    BleScanServices(final Context context){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();



        boolean permissionGranted = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if(permissionGranted){
            Toast.makeText(context, "Location Permssions Granted", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, "Location Permssions Required", Toast.LENGTH_LONG).show();
        }

    }

    public static void scanForDevices(final boolean enable, BluetoothAdapter.LeScanCallback callback) {
        final BluetoothAdapter.LeScanCallback caller = callback;
        if (enable) {
            bleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleScanner = false;
                    bleAdapter.stopLeScan(caller);
                }
            }, BLE_SCAN_PERIOD);

            bleScanner = true;
            bleAdapter.startLeScan(caller);
        } else {
            bleScanner = false;
            bleAdapter.stopLeScan(caller);
        }
    }

    public void checkBluetoothEnabled(Context context){
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            // TO DO: Figure out how to use the below two lines to prompt user to turn on their bluetooth
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(context, "Ble Not Enabled", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Ble Enabled", Toast.LENGTH_SHORT).show();
        }
    }




}
