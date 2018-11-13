# Android App Development with Bluetooth Low Energy 


## Motivation
You can see, littered around Stack Exchange, comments about how poorly Bluetooth Low Energy is documented for Android Development. I very much agree with a user who said that documentation is written for those who already understand; but very opaque to new users. Most of my learning comes from carefully, carefully walking thru [user Danasf's 2014 GitHub project](https://github.com/danasf/hm10-android-arduino), trips to [Android's official documention](https://developer.android.com/guide/topics/connectivity/bluetooth-le), and any glimmer I could find on Stack Exchange. 

I wish to point you towards writer Shahar Avigezer's September 2017 [Medium article](https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a) if you wish to learn more on Android development for Bluetooth Low Energy. The source code for that, though, does not appear to be on her GitHub yet. 

The motivation behind this project was to create a bare minimum app, easily readable, to help new users connect to an HM-10 -- HM-10 being a classic Bluetooth-to-Serial converter chip that you usually place on an Arduino in starter projects. I have included the code that I put on my Arduino Nano in order to make this work, though it is written to place on all Arduinos. 

The source code (which will eventually have documentation in it) is very chaotic. Below, I attempt to give you the proper `flow` of the bluetooth permission granting, scanning, connection, and communication events which you need to create a bluetooth low evergy app. 

An additional motivation was for me to create a minimally-viable bluetooth low energy app. As my app grows in complexity and I change IDEs / phone / what's on the arduino/hm-10 side, it will be useful to come back to this to see if the bluetooth connection mechanics work correctly. 

## Setting Up Permissions

1.	Declare necessary Bluetooth permissions in Manifest
> <uses-permission android:name="android.permission.BLUETOOTH" />
     > <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
> <uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />


2.	Declare necessary location permissions in Manifest
```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
3. Optional: Declare BLE hardware use in Manifest
```
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />
```
This is how I read Adroid's [documentation on features](https://developer.android.com/guide/topics/manifest/uses-feature-element): if you are just using this code for your senior project, and the APK is downloaded straight from your computer, declaring BLE hardware use in Manifest means nothing. I think the Google Play Store reads this line to not prevent phones without BLE hardware from downloading the app.

## Grab Permissions at Runtime
1.	Check BLE Hardware:

Some phones may not have BLE hardware on them, mainly those phones before Android SDK 4.3 / API 18 / 2013. Jellybean is SDK 4.1-4.3, so some Jellybeans support it.  In this case, I choose to close my app, alerting the user that they cannot use it. I put this on the OnCreate() of the initial activity. Note that you might see `sdk = 18`, where 18 is the API level corresponding to the 4.3 sdk version. Android Studio automagically converts the API level to the sdk version here. 

```
if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
{
    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
    finish();
}
```

2.	Check BLE permissions

Good convention is to check if you have permissions before using them. It is pretty confusing to write out what is necessary with permissions, because it depends on what API level the phone is running as well as what API you use to compile. Please see [official documentation](https://developer.android.com/guide/topics/permissions/overview) for more information on permissions. I use `targetSdkVersion 26` to compile at that level, as written in the gradle file. I use a Phoenix 3 LG phone, running API 24.  

Please note that the commented out code has not been tested to work, but I believe this is how you prompt the user to turn on their Bluetooth. 

```
final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();
        if (bleAdapter == null || !bleAdapter.isEnabled()) 
        {
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
          Toast.makeText(context, "Ble Not Enabled", Toast.LENGTH_SHORT).show();
        }
        else 
        {
          Toast.makeText(context, "Ble Enabled", Toast.LENGTH_SHORT).show();
        }
```

3.	Check location permissions:

The first time the app pulls up, there will be a popup asking for permissions for the app to use GPS. I am not sure if you already need to have GPS activated for this popup to occur. It will do nothing if you already have permissions.  

Yes, the manifest file includes fine and coarse location, but during run we only check fine location programmatically. I am not sure the logic behind this, but it works, and it breaks if you do not check fine location

```
String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
ActivityCompat.requestPermissions(this, permissions, 2);
boolean permissionGranted = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
```

## Create a Scan Callback 
We need to pass a BluetoothAdapter.LeScanCallback object to BluetoothAdapter.startLeScan to do anything with the devices found in a scan. Each device found during a scan will trigger LeScanCallback's `onLeScan` method, which I override to add that device to the list on the UI. The variable `adapter` is of a custom class derived from ArrayAdapter used to adapt devices to that list. I use this to leverage the Listview paradigm. I use this paradigm because I think it is easiest way to create dynamic lists; your milage may vary. The code also makes use of the AppCompat Activities/Toolbars (v7) paradigm, which I think is the easiest way to have a consistent toolbar at the top of each page/activity; your milage may, again, vary. Be sure to run the code that changes the Listview on the UI Thread to show changes to the user. 
```
public BluetoothAdapter.LeScanCallback mLeScanCallback = // will put this into a final variable before use
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
```

## Scan for BLE Devices
1.	Get your Bluetooth adapter
```
final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();
```
2.	Run scan
```
public static final long BLE_SCAN_PERIOD = 2000;
public static Handler bleHandler = new Handler(); 
// we will use a handler to stop the scan after a period of time, here 2 seconds
private final BluetoothAdapter.LeScanCallback callback = mLeScanCallback; 
// need to put the callback from before into a final variable
    bleHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
            bleScanner = false;
            bleAdapter.stopLeScan(callback);
        }
    }, BLE_SCAN_PERIOD);

    bleScanner = true;
    bleAdapter.startLeScan(callback);
```

## Connect to BLE Devices
1.	Get your Bluetooth adapter
```
final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
bleAdapter = bluetoothManager.getAdapter();
```
2.	Make a virtual device with your desired physical device's MAC Address
```
// My mac address is supplied by the device scan, 
// and then is shipped to another activity via an intent.
// I could certainly see why people may just hard-code their known device's mac address into the app
private String macAddress = "11:11:11:11:11:11"
BluetoothDevice device = m_bleAdapter.getRemoteDevice(macAddress);
```
3.	Poll real world for that virtual device 
```
// A gatt object is ultimately what you will use to interact with the Bluetooth stack on the HM-10. 
// All of your send and receive functions will go thru this. 
// Similar to scan callback, we need to pass a BluetoothGattCallback object to BluetoothDevice.connectGatt
// to do anything much with the gatt Bluetooth stack. 
m_gattServer = device.connectGatt(m_context,false, m_gattCallback);
```
4.	Discover Services of Gatt object
```
// the gatt callback takes over after this
    m_gattServices = gatt.discoverServices();
```

## Gatt Callback
Similar to scan callback, we need to pass a BluetoothGattCallback object to BluetoothDevice.connectGatt to do anything much with the gatt Bluetooth stack. `Gatt` stands for Generic Attribute Profile, and contains zero or more services. Each service is comprised of zero or more characteristics and zero or more other services. Also, any of the past sentence may actually be "one or more," but I am a bit hazy on this. The transmission characteristic is what we are interested. Characteristics are made of zero or more descriptors, but I'm not certain if we worry about descriptors for the HM-10 -- only the characteristic. So, we poll the gatt for all services, and search all services for the transmission characteristic hard-coded at `0000ffe1-0000-1000-8000-00805f9b34fb`. Godfrey Duke's answer from [this StackExchange](https://stackoverflow.com/questions/18699251/finding-out-android-bluetooth-le-gatt-profiles) explains that BLE characteristics are written in the form `0000XXXX-0000-1000-8000-00805f9b34fb`. Thus, when you see in HM-10 documentation that the custom characteristic is `ffe1`, it is short-hand. I refer to this as `tx`, `serial`, or `transmission` characteristic because the custom characteristic is the only one that we change, and so the only one we are interested to transmit data between the Android and HM-10; it speaks via serial protocol to the arduino. 

```
// StaticResources.java: 
// public static String HM10_SERIAL_DATA = 
//    "0000ffe1-0000-1000-8000-00805f9b34fb";
// public static String CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR =
//    "00002902-0000-1000-8000-00805f9b34fb";
// public final static String BROADCAST_NAME_SERVICES_DISCOVERED = 
//    "com.example.bluetooth.le.services_discovered";
// public final static String BROADCAST_NAME_TX_CHARATERISTIC_CHANGED =
//     UNIQUE_PACKAGE_NAME + "com.example.bluetooth.le.tx_characteristic_changed";
// public static final String BROADCAST_NAME_CONNECTION_UPDATE =
//     UNIQUE_PACKAGE_NAME + ".connection_update";
// public static final String EXTRAS_CONNECTION_STATE = "CONNECTION_STATE";
// public static final String EXTRAS_SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
// public static final String EXTRAS_TX_DATA = "TX_DATA";

// This file variables:
List<BluetoothGattService> m_gattServices;
BluetoothGattCharacteristic m_characteristicTX;
// private Context m_context = calling activity context ('this' keyword);

private final BluetoothGattCallback m_gattCallback =
        new BluetoothGattCallback() {


            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Intent intent = new Intent(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
                if (status == BluetoothGatt.GATT_SUCCESS)
                {
                    m_gattServices = gatt.getServices();
                    final UUID desiredUuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
                    String foundSuccess = "Communication Characteristic Not Found"
                    for (BluetoothGattService gattService : m_gattServices) {
                        BluetoothGattCharacteristic desiredCharacteristic = gattService.getCharacteristic(
                            desiredUuid);
                        if(desiredCharacteristic !=null)
                        {
                            m_characteristicTX = desiredCharacteristic;
                            m_gattServer.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(StaticResources.CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            m_gattServer.writeDescriptor(descriptor);

                            break;
                        }
                    }
                    if (m_characteristicTX != null)
                    {
                        foundSuccess = "Communication Characteristic Found";
                    }
                    intent.putExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED, foundSuccess);
                }
                else // Many reasons it could fail to find services
                {
                    intent.putExtra("SERVICES_DISCOVERED", "No Services Found");
                }
                m_context.sendBroadcast(intent);
            }

```

We override onServicesDiscovered, to catch the services from BluetoothGatt.discoverServices. When BluetoothGatt.GATT_SUCCESS occurs for each service, we look for the HM-10 transmission characteristic. We then use Android's broadcast paradigm to send the fact that the characteristic has or has not been found to UI. We will go over the descriptor and ENABLE_NOTIFICATION_VALUE in section 'Read from HM-10.'

Likewise onServicesDiscovered, onConnectionStateChange is overridden and uses the broadcast paradigm to alert the UI about bluetooth connection status. Last, we will override onCharacteristicChanged. We will talk more about this in section 'Read from HM-10.' 

```

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                // status: {0 = Success, other = errors}
                // newState: {0 = Disconnected, 1 = Connecting, 2 = Connected}
                Intent intent = new Intent(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, "Connected");
                    DiscoverServicesAfterDelay(gatt);

                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, "Disconnected");
                }
                else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    intent.putExtra(StaticResources.EXTRAS_CONNECTION_STATE, "Connecting");
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
            }

        };
```







## Write to HM-10
I use a button click to initiate code to write some text to my local copy of the gatt stack. I then use BluetoothGatt.writeCharacteristic to write my local copy of the characteristic to the HM-10. This allows us to think of gatt (Generic Attribute Protocol) as a map with keys (services, characteristics, and descriptors), and values. 
```
if(m_characteristicTX != null)
{
    final byte[] tx = text.getBytes();
    m_characteristicTX.setValue(tx);
    m_gattServer.writeCharacteristic(m_characteristicTX);
}
```
 

## Read from HM-10

We overrode BluetoothGattCallback.onCharacteristicChanged with code the reads the copy of the gatt stack -- and specifically the desired characteristic -- from the HM-10. See that parameter of the BluetoothGatt? Yep -- it is not the local copy. This is much different than how software handles serial ports, where the COM port is in a fixed spot and we just read the buffer. Rather, we wait for the gatt stack to come in electromagnetic particles to come on the 2.4 GHz radio frequency from the HM-10, and then we sort through the stack to find our desired characteristic. I think the HM-10 still has the 'stale data' until new data is written over it; it is not like a serial port because the act of the Android reading it will not clear out stale data. 
```
  @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                byte[] rawData = characteristic.getValue();
                String txData = new String(rawData).trim(); // toString does not work, but new String()
                ...
            }
```

We read values with BluetoothGattCallback.onCharacteristicChanged, but we also need to set up how to trigger that function. Remember that each characteristic has a variable amount of descriptors. The descriptor at "00002902-0000-1000-8000-00805f9b34fb" is called the Client Characteristic Configuration Descriptor (CCCD). Setting this to BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE means that it sends electromagnetic particles at 2.4 GHz of the gatt stack when the characteristic changes, or at least something to that effect. Then, the Android's onCharacteristicChanged can take effect. For more information on descriptors and what the value of ENABLE_NOTIFICATION_VALUE means, please see the (official documentation)[https://developer.android.com/reference/android/bluetooth/BluetoothGattDescriptor]. The line m_gattServer.writeDescriptor(descriptor) is just like writing a characteristic -- we change have changed our local copy, and this tells Android to send our local copy over 2.4 GHz radio frequency to the HM-10. 

```
  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                ...
                        if(desiredCharacteristic !=null)
                        {
                            m_characteristicTX = desiredCharacteristic;
                            m_gattServer.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            m_gattServer.writeDescriptor(descriptor);
                            break;
                        }
                    ...
            }
```


## Troubleshooting
If you are as new to Android development as I was a year ago, and you do anything less than perfectly, chances are you will run into some bizarre errors. Before you try to research the error and drive yourself crazy, please,

 - Invalidate Cache and Restart (a menu option)
 - Sync Gradle with Project (probably on the toolbar)
 - delete the folders build/ and .gradle/ (NOT gradle/!) folder while running Android IDE and re-build
 - Clean project (menu item)


Your specific Android Studio IDE version will most likely have these in separate places. These four tips help to reboot the IDE so that everything the IDE does automagically can be redone. Many errors are because this automagic process was corrupted, and there is nothing you want to change to get rid of the error. 

Keep in mind that I use Android Studio. The file structure is different for Eclipse projects, and I am not versed in how to port to that format. 

## Challenge for You
Does your Android or the HM-10 act as the 'central' or 'peripheral?' Does your HM-10 act as the 'server' or the 'client?' How do 'master' and 'slave' relate to the four terms I have just given you? I have honestly forgot the answers -- I got bluetooth to work, and do not need to remember. The answer are not as intuitive as you think, because the HM-10 documentation says it can act as a server or a client!





