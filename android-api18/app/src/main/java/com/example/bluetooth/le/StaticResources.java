package com.example.bluetooth.le;

/**
 * Created by Owner on 7/29/2018.
 */

public class StaticResources {

    public static final String UNIQUE_PACKAGE_NAME = "com.example.bluetooth.le";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_CONNECTION_STATE = "CONNECTION_STATE";
    public static final String EXTRAS_SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
    public static final String EXTRAS_TX_DATA = "TX_DATA";

    public static final String BROADCAST_NAME_CONNECTION_UPDATE =
            UNIQUE_PACKAGE_NAME + ".connection_update";
    public final static String BROADCAST_NAME_SERVICES_DISCOVERED =
            UNIQUE_PACKAGE_NAME + ".services_discovered";
    public final static String BROADCAST_NAME_TX_CHARATERISTIC_CHANGED =
            UNIQUE_PACKAGE_NAME + ".tx_characteristic_changed";

    public static String CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR =
            "00002902-0000-1000-8000-00805f9b34fb";
    public static String HM10_CONFIG = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String HM10_SERIAL_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String HM10_MANUFAC_NAME = "00002a29-0000-1000-8000-00805f9b34fb";
    public static String HM10_DEVICE_INFO = "00001800-0000-1000-8000-00805f9b34fb";


    public static final String CONNECTION_STATE_DISCONNECTED = "Disconnected";
    public static final String CONNECTION_STATE_CONNECTING = "Connecting";
    public static final String CONNECTION_STATE_CONNECTED = "Connected";

    public static String SERVICES_DISCOVERY_CHARACTERISTIC_SUCCESS =
            "Communication Characteristic Found";
    public static String SERVICES_DISCOVERY_CHARACTERISTIC_FAILURE =
            "Communication Characteristic Not Found";
    public static String SERVICES_DISCOVERY_GENERAL_FAILURE =
            "No Services Found";


    public static String COMMUNICATION_ANDROID_TO_HM10 = "A"; // use one ascii character



}
