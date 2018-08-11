# Android App with Bluetooth Low Energy 


## Motivation
You can see, littered around Stack Exchange, comments about how poorly Bluetooth Low Energy is documented for Android Development. I very much agree with a user who said that documentation is written for those who already understand; it seems very opaque to new users. Most of my learning comes from carefully walking thru [user Danasf's 2014 GitHub project](https://github.com/danasf/hm10-android-arduino), trips to [Android's official documention](https://developer.android.com/guide/topics/connectivity/bluetooth-le), and pearls of wisdom found on Stack Exchange. 

I wish to point you towards writer Shahar Avigezer's September 2017 [Medium article](https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a) if you wish to learn more on Android development for Bluetooth Low Energy. The source code for that, though, does not appear to be on her GitHub yet. 

The motivation behind this project was to create a bare minimum app, easily readable, to help new users connect to an HM-10. HM-10 being a classic Bluetooth-to-Serial converter chip that you usually place on an Arduino in starter projects. I have included the code that I put on my Arduino Nano in order to make this work, though it is written to place on all Arduinos. 

## BLE Guide that I am converting into Markdown Formatting

Setting Up Permissions

1.	Declare necessary Bluetooth permissions in Manifest
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />


1.	Declare necessary location permissions in Manifest
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

Grab Permissions at Runtime
1.	Check BLE Hardware:
Some phones may not have BLE hardware on them, mainly phones before Android SDK 4.3 / API / 2013. In this case I choose to close my app, alerting the user that they cannot use it. I put this on the OnCreate() of the initial activity. 
if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
    finish();
}

2.	Check BLE permissions
Good convention is to check if you have permissions before using them. I think a certain API level and above, Android requires you to check permissions in your app before using them. 
I know the permission checker I use API 18. I use minSdkVersion 18 in my gradle build because that was the first API to introduce BLE, and initiated 2013. Future APIs changed the permission rules, and I am not sure how they did.  
Please note that the commented out code has not been tested to work, but I believe this is how you prompt the user to turn on their Bluetooth. 

final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
          Toast.makeText(context, "Ble Not Enabled", Toast.LENGTH_SHORT).show();
        }
        else {
          Toast.makeText(context, "Ble Enabled", Toast.LENGTH_SHORT).show();
        }

3.	Check location permissions:
String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
ActivityCompat.requestPermissions(this, permissions, 2);

boolean permissionGranted = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

The first time the app pulls up, there will be a popup asking for permissions for the app to use GPS. I am not sure if you already need to have GPS activated for this popup to occur. It will do nothing if you already have permissions. 
Yes, the manifest file includes fine and coarse location, but during run we only check fine location programmatically. I am not sure the logic behind this, but it works. 




In my app I use AppCompat/Listview because I think that is easiest, you may be different.
Scan for BLE Devices
1.	Get your Bluetooth adapter
final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();

2.	Run scan

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


Scan Callback
public BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(new CustomBluetoothDeviceWrapper(device,rssi));
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };


Connect to BLE Devices
1.	Get your Bluetooth adapter
final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();

2.	Make a virtual device with your desired physical device’s MAC Address
Need mac address supplied from somewhere, i.e., your scan activity, or hard coding it. It is not in the scope of this tutorial to address how to use intents to move information (like mac address) from one activity to another, but I certainly needed a thorough understanding of that to bring you this.



BluetoothDevice device = m_bleAdapter.getRemoteDevice(macAddress);

3.	Poll real world for that virtual device 
A gatt object is ultimately what you will use to interact with the Bluetooth stack on the HM-10. All of your send and receive functions will go thru this. The Callback will be spoken about after the fact. 


m_gattServer = device.connectGatt(m_context,false, m_gattCallback);

4.	Discover Services of Gatt object

    m_gattServices = gatt.discoverServices();


5.	Get Characteristic from Services

// Need to wait until services become populated from previous step
    m_gattServices = gatt.getServices();
    m_characteristicTX = FindCharacteristic

final UUID desiredUuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
for (BluetoothGattService gattService : m_gattServices) {
    BluetoothGattCharacteristic desiredCharacteristic = gattService.getCharacteristic(
            desiredUuid);
    if(desiredCharacteristic !=null)
    {
        m_characteristicTX = desiredCharacteristic;
        break;
    }
}



Gatt is made up of a variable amount of services, each service is made up of a variable amount of characteristics, and each characteristic is made up of a variable amount of descriptors. Because we know we only care about the characteristic  "0000ffe1-0000-1000-8000-00805f9b34fb" from the HM-10 to send and receive info with the Arduino, we can ignore all of the other noise. We loop thru all available services and get the characteristic that we want. While we could write or read other characteristics, THIS characteristic will match the serial communication with the Arduino. 

Read from HM-10
6.	Set up Reading Ability

m_gattServer.setCharacteristicNotification(characteristic, true);
// Enable the local machine to watch changes to this characteristic
// Then, change the peripheral to notify observers of changes in its payload. 
    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    m_gattServer.writeDescriptor(descriptor);
Do not think about Bluetooth in terms of a serial port, where you send data in a buffer and read that when you are ready (say by reading if any available data on a 0.1 second continuous loop). Think rather that Bluetooth an only notify an observer of a new value change. There probably is a way to read current value, but that is beyond my knowledge.  
In order to set up reading, we need to set up both the local (Android) and peripheral (HM-10). The call to setCharacteristicNotification  makes the local an observer in changes to this characteristic. Next, we configure the peripheral. Remember that each characteristic has a variable amount of descriptors? Well, "00002902-0000-1000-8000-00805f9b34fb" is called the Client Characteristic Configuration Descriptor (CCCD). Setting this to BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE means that it notifies clients/observers of a change in payload. Finally, we need to the characteristic to have an effect.  
7.	Act on new values
See onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)in BluetoothGattCallback m_gattCallback

Write to HM-10
8.	Write to a characteristic

if(m_characteristicTX != null)
{
    final byte[] tx = text.getBytes();
    m_characteristicTX.setValue(tx);
    m_gattServer.writeCharacteristic(m_characteristicTX);
}

Setting the value of the characteristic changes your local copy (Android). Need to write it to the peripheral (HM-10). Think of the characteristic as one big key, and your value as a key-value. The key allows writing to target the characteristic among the hierarchy of uuids that comprise service/characteristic/descriptor hierarchy. This is the same reason we needed to write the CCCD for readability. 


Gatt Callback

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









 


