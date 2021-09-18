package com.example.monitor;

import android.os.ParcelUuid;

import java.util.UUID;

public class DeviceProtocol {

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

    public static final byte READ_BYTE_COMMAND              = 0x01;
    public static final byte READ_WORD_COMMAND              = 0x02;
    public static final byte READ_DWORD_COMMAND             = 0x03;
    public static final byte READ_READWRITE_BYTE_COMMAND    = 0x04;
    public static final byte READ_READWRITE_WORD_COMMAND    = 0x05;
    public static final byte READ_READWRITE_DWORD_COMMAND   = 0x06;
    public static final byte WRITE_READWRITE_BYTE_COMMAND   = 0x07;
    public static final byte WRITE_READWRITE_WORD_COMMAND   = 0x08;
    public static final byte WRITE_READWRITE_DWORD_COMMAND  = 0x09;
    public static final byte SET_DATETIME_COMMAND           = 0x0A;
    public static final byte GET_DATETIME_COMMAND           = 0x0B;
    public static final byte READ_JOURNAL_EVENT_COMMAND     = 0x0C;
    public static final byte DFU_MODE_COMMAND               = 0x0D;
    public static final byte CLEAN_JOURNAL_EVENT_COMMAND    = 0x0E;
    public static final byte RETURN_TO_FACTORY_COMMAND      = 0x0F;
    public static final byte PASS_PASSWORD_COMMAND          = 0x10;
    public static final byte NEW_PASSWORD_COMMAND           = 0x11;
    public static final byte READ_READTABLE_DATA_GROUP      = 0x12;
    public static final byte READ_READWRITETABLE_DATA_GROUP = 0x13;

    // ***********************  ERROR CODES  ***********************
    public static final byte UNSUPPORTED_FUNCTION_CODE_ERROR    = 0x01;
    public static final byte ILLEGAL_DATA_ADDRESS_ERROR         = 0x02;
    public static final byte ILLEGAL_WRITE_DATA_VALUE_ERROR     = 0x03;
    public static final byte DATA_TYPE_ON_ADDRESS_ERROR         = 0x04;
    public static final byte REQUESTED_DATA_CORRUPTED_ERROR     = 0x05;
    public static final byte WRONG_PASSWORD_ERROR               = 0x06;
    public static final byte NO_ACCESS_ERROR                    = 0x07;

    // ***********************  "READ" TABLE  *******************
    public static final int SPEED               = 1;
    public static final int CURRENT             = 2;
    public static final int BUTTERY_VOLTAGE     = 3;
    public static final int ENERGY_CONSUMPTION  = 7;
    public static final int ENERGY_REMAINS      = 11;
    public static final int COMMON_MILEAGE      = 15;
    public static final int MILEAGE             = 16;
    public static final int IEC_TEMPERATURE     = 22;
    public static final int STATE_REGISTER_1    = 28;

    public static final int SPEED_SIZE              = 2;
    public static final int CURRENT_SIZE            = 2;
    public static final int BUTTERY_VOLTAGE_SIZE    = 2;
    public static final int ENERGY_CONSUMPTION_SIZE = 4;
    public static final int ENERGY_REMAINS_SIZE     = 4;
    public static final int COMMON_MILEAGE_SIZE     = 4;
    public static final int MILEAGE_SIZE            = 4;
    public static final int IEC_TEMPERATURE_SIZE    = 4;
    public static final int STATE_REGISTER_1_SIZE   = 4;

    // state register 1 byte number masks
    public static final int DEVICE_ON                   = 0x01;
    public static final int FIRST_SPEED                 = 0x04;
    public static final int SECOND_SPEED                = 0x08;
    public static final int THIRD_SPEED                 = 0x10;
    public static final int ENGINE_BRAKE                = 0x20;
    public static final int REVERSE_ENABLED             = 0x80;

    public static final int BY_FOOT_MOVING_MODE         = 0x01;
    public static final int SPEED_SENSOR_DISABLED_MODE  = 0x04;
    public static final int BRAKE                       = 0x08;
    public static final int TURN_LEFT                   = 0x10;
    public static final int TURN_RIGHT                  = 0x20;
    public static final int POSITION_LIGHT              = 0x40;
    public static final int HEADLIGHT                   = 0x80;

    public static final int DECO_LIGHT                  = 0x01;

    // ***********************  "READ AND WRITE" TABLE  *******************
    public static final int MOMENT_REGULATION_PROHIBIT_MODE     = 51;
    public static final int ONLY_MOMENT_REGULATION_MODE         = 52;
    public static final int FIRST_SPEED_VALUE                   = 53;
    public static final int SECOND_SPEED_VALUE                  = 54;
    public static final int THIRD_SPEED_VALUE                   = 55;
    public static final int MAX_SPEED_UP_CURRENT                = 56;
    public static final int MAX_SPEED_DOWN_CURRENT              = 57;
    public static final int ENGINE_BRAKE_PROHIBITED             = 58;
    public static final int MAX_FORWARD_SPEED                   = 59;
    public static final int MAX_BACKWARD_SPEED                  = 60;
    public static final int MAX_FORWARD_SPEED_ACCELERATION      = 61;
    public static final int MAX_BACKWARD_SPEED_ACCELERATION     = 62;
    public static final int MAX_FORWARD_MOMENT_ACCELERATION     = 63;
    public static final int MAX_BACKWARD_MOMENT_ACCELERATION    = 64;
    public static final int SPEED_SENSOR_DISABLED_MODE_VALUE    = 65;
    public static final int ENGINE_SPEED_LIMIT_BRAKE_MODE       = 66;
    public static final int ABS_ASR_MODE                        = 91;
    public static final int LIGHT_AUTO_MODE                     = 81;
    public static final int LIGHT_LEVEL                         = 82;
    public static final int RESET_MILEAGE                       = 202;
    public static final int ENABLE_HEADLIGHT                    = 203;
    public static final int ENABLE_LIGHT                        = 204;

    public static final int MOMENT_REGULATION_PROHIBIT_MODE_SIZE     = 1;
    public static final int ONLY_MOMENT_REGULATION_MODE_SIZE         = 1;
    public static final int FIRST_SPEED_VALUE_SIZE                   = 4;
    public static final int SECOND_SPEED_VALUE_SIZE                  = 4;
    public static final int THIRD_SPEED_VALUE_SIZE                   = 4;
    public static final int MAX_SPEED_UP_CURRENT_SIZE                = 4;
    public static final int MAX_SPEED_DOWN_CURRENT_SIZE              = 4;
    public static final int ENGINE_BRAKE_PROHIBITED_SIZE             = 1;
    public static final int MAX_FORWARD_SPEED_SIZE                   = 4;
    public static final int MAX_BACKWARD_SPEED_SIZE                  = 4;
    public static final int MAX_FORWARD_SPEED_ACCELERATION_SIZE      = 4;
    public static final int MAX_BACKWARD_SPEED_ACCELERATION_SIZE     = 4;
    public static final int MAX_FORWARD_MOMENT_ACCELERATION_SIZE     = 4;
    public static final int MAX_BACKWARD_MOMENT_ACCELERATION_SIZE    = 4;
    public static final int SPEED_SENSOR_DISABLED_MODE_VALUE_SIZE    = 1;
    public static final int ENGINE_SPEED_LIMIT_BRAKE_MODE_SIZE       = 1;
    public static final int ABS_ASR_MODE_SIZE                        = 1;
    public static final int LIGHT_AUTO_MODE_SIZE                     = 1;
    public static final int LIGHT_LEVEL_SIZE                         = 1;
    public static final int RESET_MILEAGE_SIZE                       = 1;
    public static final int ENABLE_HEADLIGHT_SIZE                    = 1;
    public static final int ENABLE_LIGHT_SIZE                        = 1;


    // ***********************  TABLES  ***********************************
    public static final int READ_TABLE          = 1;
    public static final int READ_WRITE_TABLE    = 2;

    // ***********************  SWITCH MODES  *****************************
    public static final int MODE_ON = 1;
    public static final int MODE_OF = 0;
}
