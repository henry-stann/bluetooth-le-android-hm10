package com.example.bluetooth.le;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Owner on 8/3/2018.
 */

public class CustomBluetoothDeviceWrapper {

    BluetoothDevice m_device;
    int m_rssi;

    CustomBluetoothDeviceWrapper(BluetoothDevice device, int rssi)
    {
        m_device = device;
        m_rssi = rssi;
    }

    // Wrapper ensures name will have default string value of unknown.
    public String getName(){
        String friendlyName = "unknown";
        if(m_device.getName() != null)
        {
            friendlyName = m_device.getName();
        }
        return friendlyName;
    }

    public String getAddress(){
        return m_device.getAddress();
    }

    // Wrapper ensures rssi value can be taken with the bluetooth device
    public int getRssi(){
        return m_rssi;
    }

}
