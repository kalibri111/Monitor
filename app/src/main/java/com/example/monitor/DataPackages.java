package com.example.monitor;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class DataPackages {
    public static final List<DataView> MainFragmentDataPackage = Arrays.asList(
            new DataView(DeviceInfo.SPEED,              DeviceInfo.SPEED_SIZE           , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.CURRENT,            DeviceInfo.CURRENT_SIZE         , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.MILEAGE,            DeviceInfo.MILEAGE_SIZE         , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.COMMON_MILEAGE,     DeviceInfo.COMMON_MILEAGE_SIZE  , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.IEC_TEMPERATURE,    DeviceInfo.IEC_TEMPERATURE_SIZE , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.STATE_REGISTER_1,   DeviceInfo.STATE_REGISTER_1_SIZE, DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.ENERGY_REMAINS,     DeviceInfo.ENERGY_REMAINS_SIZE  , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.ABS_ASR_MODE,       DeviceInfo.ABS_ASR_MODE_SIZE    , DeviceInfo.READ_WRITE_TABLE)
    );

    public static final List<DataView> SettingsFragmentDataPackage = Arrays.asList(
            new DataView(DeviceInfo.MAX_SPEED_UP_CURRENT            , DeviceInfo.MAX_SPEED_UP_CURRENT_SIZE              , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_SPEED_DOWN_CURRENT          , DeviceInfo.MAX_SPEED_DOWN_CURRENT_SIZE            , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.ENGINE_SPEED_LIMIT_BRAKE_MODE   , DeviceInfo.ENGINE_SPEED_LIMIT_BRAKE_MODE_SIZE     , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.STATE_REGISTER_1                , DeviceInfo.STATE_REGISTER_1_SIZE                  , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.MAX_FORWARD_SPEED               , DeviceInfo.MAX_FORWARD_SPEED_SIZE                 , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_BACKWARD_SPEED              , DeviceInfo.MAX_BACKWARD_SPEED_SIZE                , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_FORWARD_SPEED_ACCELERATION  , DeviceInfo.MAX_FORWARD_SPEED_ACCELERATION_SIZE    , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_BACKWARD_SPEED_ACCELERATION , DeviceInfo.MAX_BACKWARD_SPEED_ACCELERATION_SIZE   , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_FORWARD_MOMENT_ACCELERATION , DeviceInfo.MAX_FORWARD_MOMENT_ACCELERATION_SIZE   , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.MAX_BACKWARD_MOMENT_ACCELERATION, DeviceInfo.MAX_BACKWARD_MOMENT_ACCELERATION_SIZE  , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.LIGHT_AUTO_MODE                 , DeviceInfo.LIGHT_AUTO_MODE_SIZE                   , DeviceInfo.READ_WRITE_TABLE),
            new DataView(DeviceInfo.LIGHT_LEVEL                     , DeviceInfo.LIGHT_LEVEL_SIZE                       , DeviceInfo.READ_WRITE_TABLE)
    );

    public static final List<DataView> InfoFragmentDataPackage = Arrays.asList(
            new DataView(DeviceInfo.COMMON_MILEAGE      , DeviceInfo.COMMON_MILEAGE_SIZE    , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.MILEAGE             , DeviceInfo.MILEAGE_SIZE           , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.BUTTERY_VOLTAGE     , DeviceInfo.BUTTERY_VOLTAGE_SIZE   , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.IEC_TEMPERATURE     , DeviceInfo.IEC_TEMPERATURE_SIZE   , DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.ENERGY_CONSUMPTION  , DeviceInfo.ENERGY_CONSUMPTION_SIZE, DeviceInfo.READ_TABLE),
            new DataView(DeviceInfo.ENERGY_REMAINS      , DeviceInfo.ENERGY_REMAINS_SIZE    , DeviceInfo.READ_TABLE)
    );

    private int current;

    public DataPackages() {
        this.current = 0;
    }

    public void next(@NonNull List<DataView> dataViews) {
        if (current < dataViews.size()) {
            current += 1;
        } else {
            current = 0;
        }
    }

    public DataView getMainFragmentDataView() {
        return MainFragmentDataPackage.get(this.current);
    }

    public DataView getSettingsFragmentDataView() {
        return SettingsFragmentDataPackage.get(this.current);
    }

    public DataView getInfoFragmentDataView() {
        return InfoFragmentDataPackage.get(this.current);
    }
}
