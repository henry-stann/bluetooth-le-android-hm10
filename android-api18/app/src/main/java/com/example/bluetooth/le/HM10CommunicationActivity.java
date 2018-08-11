package com.example.bluetooth.le;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * I need to track down where I got this code base from. If from Google Android, it has Apache 2.0 license.
 */

public class HM10CommunicationActivity extends AppCompatActivity {

    private TextView mHasSerial;
    private TextView textSerialConnection;
    private String mDeviceName;
    private String m_deviceAddress;
    private BleConnectionService m_bleConnectionService;
    private boolean m_hasSerial;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hm10_communication);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_NAME);
        m_deviceAddress = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_ADDRESS);
        m_bleConnectionService = new BleConnectionService(this, m_deviceAddress);

        Toolbar toolbar = findViewById(R.id.app_bar_hm10_communication); // declare the toolbar.
        TextView textDeviceName = toolbar.findViewById(R.id.toolbar_device_name);
        TextView textDeviceAddress = toolbar.findViewById(R.id.toolbar_device_address);

        textDeviceName.setText(mDeviceName);
        textDeviceAddress.setText(m_deviceAddress);

        setSupportActionBar(toolbar); // Make toolbar visible on activity. Xml is very minimalistic
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textSerialConnection = findViewById(R.id.bluetooth_serial_cxn_value);
        textSerialConnection.setText(StaticResources.CONNECTION_STATE_CONNECTING);
        m_hasSerial = false;

        IntentFilter filterMaster =new IntentFilter();
        filterMaster.addAction(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED);
        registerReceiver(m_bleBroadcastReceiver, filterMaster);

    }


    public void communication_Button_Click(View v)
    {
        m_bleConnectionService.writeToBluetoothSerial(StaticResources.COMMUNICATION_ANDROID_TO_HM10);

    }
    public void connect_Button_Click(View v)
    {
        textSerialConnection.setText(StaticResources.CONNECTION_STATE_CONNECTING);
        m_bleConnectionService.connect(m_deviceAddress);

    }

    private final BroadcastReceiver m_bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String broadcastName = intent.getAction();
            Log.i("Broadcast Receiver",
                    "Recieved Broadcast name = " + broadcastName
            );
            switch(broadcastName)
            {
                case StaticResources.BROADCAST_NAME_CONNECTION_UPDATE:
                    final String connection = intent.getStringExtra(StaticResources.EXTRAS_CONNECTION_STATE);
//                    textSerialConnection.setText(connection);
                    break;
                case StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED:
                    final String serial = intent.getStringExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED);
                    textSerialConnection.setText(serial);
                    if (serial == StaticResources.SERVICES_DISCOVERY_CHARACTERISTIC_SUCCESS)
                    {
                        m_hasSerial = true;
                    }
                    break;
                case StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED:
                    final String txData = intent.getStringExtra(StaticResources.EXTRAS_TX_DATA);
//                    final String txData = "Tx data received from HM10";
                    Toast.makeText(context, txData, Toast.LENGTH_SHORT).show();
                    Log.i("Broadcast Received",
                            "TxData = " + txData + ";");
                    break;
            }
        }
    };








}
