package com.example.monitor;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataPackages {
    public static final List<DataView> MainFragmentDataPackage = Arrays.asList(
            new DataView(DeviceProtocol.SPEED,              DeviceProtocol.SPEED_SIZE           , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.CURRENT,            DeviceProtocol.CURRENT_SIZE         , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.MILEAGE,            DeviceProtocol.MILEAGE_SIZE         , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.COMMON_MILEAGE,     DeviceProtocol.COMMON_MILEAGE_SIZE  , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.IEC_TEMPERATURE,    DeviceProtocol.IEC_TEMPERATURE_SIZE , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.STATE_REGISTER_1,   DeviceProtocol.STATE_REGISTER_1_SIZE, DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.ENERGY_REMAINS,     DeviceProtocol.ENERGY_REMAINS_SIZE  , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.ABS_ASR_MODE,       DeviceProtocol.ABS_ASR_MODE_SIZE    , DeviceProtocol.READ_WRITE_TABLE)
    );

    public static final List<DataView> SettingsFragmentDataPackage = Arrays.asList(
            new DataView(DeviceProtocol.MAX_SPEED_UP_CURRENT            , DeviceProtocol.MAX_SPEED_UP_CURRENT_SIZE              , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_SPEED_DOWN_CURRENT          , DeviceProtocol.MAX_SPEED_DOWN_CURRENT_SIZE            , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.ENGINE_SPEED_LIMIT_BRAKE_MODE   , DeviceProtocol.ENGINE_SPEED_LIMIT_BRAKE_MODE_SIZE     , DeviceProtocol.READ_WRITE_TABLE),
//            new DataView(DeviceProtocol.STATE_REGISTER_1                , DeviceProtocol.STATE_REGISTER_1_SIZE                  , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.MAX_FORWARD_SPEED               , DeviceProtocol.MAX_FORWARD_SPEED_SIZE                 , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_BACKWARD_SPEED              , DeviceProtocol.MAX_BACKWARD_SPEED_SIZE                , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_FORWARD_SPEED_ACCELERATION  , DeviceProtocol.MAX_FORWARD_SPEED_ACCELERATION_SIZE    , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_BACKWARD_SPEED_ACCELERATION , DeviceProtocol.MAX_BACKWARD_SPEED_ACCELERATION_SIZE   , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_FORWARD_MOMENT_ACCELERATION , DeviceProtocol.MAX_FORWARD_MOMENT_ACCELERATION_SIZE   , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.MAX_BACKWARD_MOMENT_ACCELERATION, DeviceProtocol.MAX_BACKWARD_MOMENT_ACCELERATION_SIZE  , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.LIGHT_AUTO_MODE                 , DeviceProtocol.LIGHT_AUTO_MODE_SIZE                   , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.LIGHT_LEVEL                     , DeviceProtocol.LIGHT_LEVEL_SIZE                       , DeviceProtocol.READ_WRITE_TABLE),
            new DataView(DeviceProtocol.ENGINE_BRAKE_PROHIBITED         , DeviceProtocol.ENGINE_BRAKE_PROHIBITED_SIZE           , DeviceProtocol.READ_WRITE_TABLE)
    );

    public static final List<DataView> InfoFragmentDataPackage = Arrays.asList(
            new DataView(DeviceProtocol.COMMON_MILEAGE      , DeviceProtocol.COMMON_MILEAGE_SIZE    , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.MILEAGE             , DeviceProtocol.MILEAGE_SIZE           , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.BUTTERY_VOLTAGE     , DeviceProtocol.BUTTERY_VOLTAGE_SIZE   , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.IEC_TEMPERATURE     , DeviceProtocol.IEC_TEMPERATURE_SIZE   , DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.ENERGY_CONSUMPTION  , DeviceProtocol.ENERGY_CONSUMPTION_SIZE, DeviceProtocol.READ_TABLE),
            new DataView(DeviceProtocol.ENERGY_REMAINS      , DeviceProtocol.ENERGY_REMAINS_SIZE    , DeviceProtocol.READ_TABLE)
    );

    private int current;
    private boolean end = false;

    public DataPackages() {
        this.current = 0;
        Collections.sort(MainFragmentDataPackage, new Comparator<DataView>() {
            @Override
            public int compare(DataView o1, DataView o2) {
                return o1.address - o2.address;
            }
        });

        Collections.sort(SettingsFragmentDataPackage, new Comparator<DataView>() {
            @Override
            public int compare(DataView o1, DataView o2) {
                return o1.address - o2.address;
            }
        });

        Collections.sort(InfoFragmentDataPackage, new Comparator<DataView>() {
            @Override
            public int compare(DataView o1, DataView o2) {
                return o1.address - o2.address;
            }
        });
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void next(@NonNull List<DataView> dataViews) {
        if (current < dataViews.size() - 1) {
            current += 1;
        } else {
            current = 0;
            end = true;
        }
    }

    public boolean isEnd() {
        return end;
    }

    public void refresh() {
        this.end = false;
        this.current = 0;
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
