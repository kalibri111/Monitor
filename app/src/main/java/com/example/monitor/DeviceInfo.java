package com.example.monitor;

import android.os.ParcelUuid;

import java.util.UUID;

public class DeviceInfo {

    // ***********************  ACTIONS   ***********************

    public static final String TX_DATA_DETECTED = "com.monitor.action.new_tx_data";

    // ***********************   EXTRAS   ***********************

    public static final String TX_DATA_DETECTED_EXTRA = "com.monitor.action.new_tx_data.extra";

    // ***********************  SERVICES  ***********************

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    // ***********************  PROTOCOL  ***********************

    public static final byte PASS_PASSWORD_COMMAND = 0x10;
    public static final byte WRONG_PASSWORD_ANSWER = 0x06;
    public static final byte ERROR_PASSWORD_ANSWER = 0x09;
}
