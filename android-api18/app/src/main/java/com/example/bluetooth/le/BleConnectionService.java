package com.example.bluetooth.le;

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
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * Created by Owner on 8/4/2018.
 */

public class BleConnectionService {

    private BluetoothManager m_bleManager;
    private BluetoothAdapter m_bleAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt m_gattServer;
    private Context m_context;
    List<BluetoothGattService> m_gattServices;
    BluetoothGattCharacteristic m_characteristicTX;

    BleConnectionService(Context context, String macAddress){
        m_context = context;
        final BluetoothManager m_bleManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        m_bleAdapter = m_bleManager.getAdapter(); // Need check for null here!
        connect(macAddress);
    }

    private final BluetoothGattCallback m_gattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    // Status {0 = Success, other = errors}
                    // NewStatus {0 = Disconnected, 1 = Connecting, 2 = Connected}
                    Log.i("onConnectionStateChange",
                            "Status = " + Integer.toString(status) +
                                    ". New State = " + Integer.toString(newState)
                    );
                    Intent intent = new Intent(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, StaticResources.CONNECTION_STATE_CONNECTED);
                        DiscoverServicesAfterDelay(gatt);

                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, StaticResources.CONNECTION_STATE_DISCONNECTED);
                    }
                    else if (newState == BluetoothProfile.STATE_CONNECTING) {
                        intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, StaticResources.CONNECTION_STATE_CONNECTING);
                    }
                    m_context.sendBroadcast(intent);
                }


                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Intent intent = new Intent(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
                    Log.i("onServicesDiscovered",
                            "Status = " + Integer.toString(status)
                    );
                    if (status == BluetoothGatt.GATT_SUCCESS)
                    {
                        m_gattServices = gatt.getServices();
                        m_characteristicTX = FindCharacteristic(StaticResources.HM10_SERIAL_DATA, m_gattServices);
                        String foundSuccess = StaticResources.SERVICES_DISCOVERY_CHARACTERISTIC_FAILURE;
                        if (m_characteristicTX != null)
                        {
                            foundSuccess = StaticResources.SERVICES_DISCOVERY_CHARACTERISTIC_SUCCESS;
                        }
                            intent.putExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED,
                                    foundSuccess);
                    }
                    else // Many reasons it could fail to find services
                    {
                        intent.putExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED,
                                StaticResources.SERVICES_DISCOVERY_GENERAL_FAILURE);
                    }
                    m_context.sendBroadcast(intent);
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    byte[] rawData = characteristic.getValue();
                    String txData = new String(rawData).trim(); // toString does not work, but new String()
                    Intent intent = new Intent(StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED);
                    intent.putExtra(StaticResources.EXTRAS_TX_DATA, txData);
                    m_context.sendBroadcast(intent);
                    Log.i("onCharacteristicChanged",
                            "TxData = " + txData + ";");
                }

            };

    private BluetoothGattCharacteristic FindCharacteristic(String uuidString, List<BluetoothGattService> possibleServices) {
        final UUID desiredUuid = UUID.fromString(uuidString);
        for (BluetoothGattService gattService : possibleServices) {
            BluetoothGattCharacteristic desiredCharacteristic = gattService.getCharacteristic(
                    desiredUuid);
            if(desiredCharacteristic !=null)
            {
                return desiredCharacteristic;
            }
        }
        return null;
    }

    public void connect(String macAddress)
    {
        BluetoothDevice device = m_bleAdapter.getRemoteDevice(macAddress);
        m_gattServer = device.connectGatt(m_context,false, m_gattCallback);
    }


    private void DiscoverServicesAfterDelay(BluetoothGatt gatt) {
        try {
            Thread.sleep(600); // Is not running on UI thread.
            gatt.discoverServices();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(m_context, "Could not find BLE services.", Toast.LENGTH_SHORT).show();

        }
    }

    public void EnableReadNotifications(BluetoothGattCharacteristic characteristic) {
        m_gattServer.setCharacteristicNotification(characteristic, true);
        // Enable the local machine to watch changes to this characteristic
        // Then, change the peripheral to notify observers of changes in its payload.
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(StaticResources.CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            m_gattServer.writeDescriptor(descriptor);
    }


    public void writeToBluetoothSerial(String text)
    {
        if(m_characteristicTX != null)
        {
            final byte[] tx = text.getBytes();
            m_characteristicTX.setValue(tx);
            m_gattServer.writeCharacteristic(m_characteristicTX);
            EnableReadNotifications(m_characteristicTX); // Put when I initially find the services.
        }
        else
        {
            Toast.makeText(m_context, "Tried to write without having connection", Toast.LENGTH_SHORT).show();
        }

    }

}
