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

    public static final byte READ_BYTE_COMMAND = 0x01;
    public static final byte READ_WORD_COMMAND = 0x02;
    public static final byte READ_DWORD_COMMAND = 0x03;
    public static final byte READ_READWRITE_BYTE_COMMAND = 0x04;
    public static final byte READ_READWRITE_WORD_COMMAND = 0x05;
    public static final byte READ_READWRITE_DWORD_COMMAND = 0x06;
    public static final byte WRITE_READWRITE_BYTE_COMMAND = 0x07;
    public static final byte WRITE_READWRITE_WORD_COMMAND = 0x08;
    public static final byte WRITE_READWRITE_DWORD_COMMAND = 0x09;
    public static final byte SET_DATETIME_COMMAND = 0x0A;
    public static final byte GET_DATETIME_COMMAND = 0x0B;
    public static final byte READ_JOURNAL_EVENT_COMMAND = 0x0C;
    public static final byte DFU_MODE_COMMAND = 0x0D;
    public static final byte CLEAN_JOURNAL_EVENT_COMMAND = 0x0E;
    public static final byte RETURN_TO_FACTORY_COMMAND = 0x0F;
    public static final byte PASS_PASSWORD_COMMAND = 0x10;
    public static final byte NEW_PASSWORD_COMMAND = 0x11;
    public static final byte READ_INDICATION_DATA_GROUP = 0x12;
    public static final byte READ_ENGINE_DATA_GROUP = 0x13;
    public static final byte READ_SPEED_DATA_GROUP = 0x14;
    public static final byte READ_LIGHT_DATA_GROUP = 0x15;
    public static final byte READ_SPECIFIC_DATA_GROUP = 0x16;

    // ***********************  ERROR CODES  ***********************
    public static final byte UNSUPPORTED_FUNCTION_CODE_ERROR = 0x01;
    public static final byte ILLEGAL_DATA_ADDRESS_ERROR = 0x02;
    public static final byte ILLEGAL_WRITE_DATA_VALUE_ERROR = 0x03;
    public static final byte DATA_TYPE_ON_ADDRESS_ERROR = 0x04;
    public static final byte REQUESTED_DATA_CORRUPTED_ERROR = 0x05;
    public static final byte WRONG_PASSWORD_ERROR = 0x06;
    public static final byte NO_ACCESS_ERROR = 0x07;

    // ***********************  VALUE GROUP ADDRESSES  *******************
    public static final int SPEED_GROUP_ADDRESS = 0;
    public static final int CURRENT_GROUP_ADDRESS = 0;
    public static final int ENERGY_GROUP_ADDRESS = 0;
    public static final int ABS_GROUP_ADDRESS = 0;
    public static final int ASR_GROUP_ADDRESS = 0;
    public static final int FIRST_SPEED_ENABLED_GROUP_ADDRESS = 0;
    public static final int SECOND_SPEED_ENABLED_GROUP_ADDRESS = 0;
    public static final int THIRD_SPEED_ENABLED_GROUP_ADDRESS = 0;
    public static final int BRAKE_GROUP_ADDRESS = 0;
    public static final int LIGHT_GROUP_ADDRESS = 0;
    public static final int DEVICE_ON_GROUP_ADDRESS = 0;
    public static final int COMMON_MILEAGE_INDICATION_GROUP_ADDRESS = 0;
    public static final int MILEAGE_INDICATION_GROUP_ADDRESS = 0;
    public static final int IEC_TEMPERATURE_INDICATION_GROUP_ADDRESS = 0;
    public static final int CAN_STATE_INDICATION_GROUP_ADDRESS = 0;
    public static final int MAX_SPEEDUP_CURRENT_GROUP_ADDRESS = 0;
    public static final int MAX_SPEEDDOWN_CURRENT_GROUP_ADDRESS = 0;
    public static final int ENGINE_BRAKE_GROUP_ADDRESS = 0;
    public static final int ENGINE_SPEEDLIMIT_BRAKE_GROUP_ADDRESS = 0;
    public static final int MAX_FORWARD_SPEED_GROUP_ADDRESS = 0;
    public static final int MAX_BACKWARD_SPEED_GROUP_ADDRESS = 0;
    public static final int MAX_FORWARD_SPEED_ACCELERATION_GROUP_ADDRESS = 0;
    public static final int MAX_BACKWARD_SPEED_ACCELERATION_GROUP_ADDRESS = 0;
    public static final int MAX_FORWARD_MOMENT_ACCELERATION_GROUP_ADDRESS = 0;
    public static final int MAX_BACKWARD_MOMENT_ACCELERATION_GROUP_ADDRESS = 0;
    public static final int LIGHT_AUTO_GROUP_ADDRESS = 0;
    public static final int LIGHT_MODE_GROUP_ADDRESS = 0;
    public static final int COMMON_MILEAGE_GROUP_ADDRESS = 0;
    public static final int MILEAGE_GROUP_ADDRESS = 0;
    public static final int IEC_TEMPERATURE_GROUP_ADDRESS = 0;
    public static final int CAN_STATE_GROUP_ADDRESS = 0;
    public static final int ENERGY_CONSUMPTION_GROUP_ADDRESS = 0;
    public static final int ENERGY_REMAINS_GROUP_ADDRESS = 0;
    public static final int BMS_STATE_GROUP_ADDRESS = 0;

}
