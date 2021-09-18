package com.example.monitor;

import static com.example.monitor.DataManager.extractErrorCommand;
import static com.example.monitor.DataManager.isErrorResponse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.welie.blessed.BluetoothPeripheral;

import java.util.Arrays;


public class InfoFragment extends Fragment {
    private BluetoothLayer  bluetoothLayer;

    DataPackages dataPackages = new DataPackages();
    OptimizedRequest optimizedRequest = null;

    TextView commonMileageView;
    TextView mileageView;
    TextView batteryVoltageView;
    TextView iecTemperatureView;
    TextView energyConsumptionView;
    TextView energyRemainsView;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public InfoFragment() {
        // Required empty public constructor
    }

    public static InfoFragment newInstance(String param1, String param2) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        commonMileageView       = view.findViewById(R.id.generalRunValue);
        mileageView             = view.findViewById(R.id.RunValue);
        batteryVoltageView      = view.findViewById(R.id.BatteryVoltageValue);
        iecTemperatureView      = view.findViewById(R.id.TemperatureValue);
        energyConsumptionView   = view.findViewById(R.id.ConsumptionValue);
        energyRemainsView       = view.findViewById(R.id.BatEnergyValue);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        DataManager.InfoFragmentRequests = false;
        BluetoothPeripheral connectedFromSettingsFragment = ((MainActivity) getActivity()).getConnectedDevice();
        getContext().registerReceiver(txReceiver, new IntentFilter(DeviceProtocol.TX_DATA_DETECTED));
        if (connectedFromSettingsFragment != null) {
            bluetoothLayer = BluetoothLayer.getInstance(getContext());
            bluetoothLayer.setBoundedDevice(connectedFromSettingsFragment.getAddress());

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DataManager.InfoFragmentRequests = true;
                    optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.InfoFragmentDataPackage);
                    DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.INFO_FRAGMENT_DATA_PACKAGE);
//                    DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.INFO_FRAGMENT_DATA_PACKAGE);
                }
            }, 200);

        }
    }

    @Override
    public void onStop() {
        super.onStop();

        DataManager.InfoFragmentRequests = false;
        getContext().unregisterReceiver(txReceiver);
    }

    private void refreshRenderedData(byte[] actualValues, DataView currentDataView) {
//        DataView currentDataView = dataPackages.getInfoFragmentDataView();
        Log.e("Monitor", String.format("InfoFragment refresh %s", BytesInterpret.dumpBytes(actualValues)));

            switch (currentDataView.address) {

                case DeviceProtocol.MILEAGE: {
                    float val = BytesInterpret.toFloat(actualValues); // LE BE ?
                    mileageView.setText(String.valueOf(val));
                    break;
                }

                case DeviceProtocol.COMMON_MILEAGE: {
                    float val = BytesInterpret.toFloat(actualValues); // LE BE ?
                    commonMileageView.setText(String.valueOf(val));
                    break;
                }

                case DeviceProtocol.IEC_TEMPERATURE: {
                    float val = BytesInterpret.toFloat(actualValues); // LE BE ?
                    iecTemperatureView.setText(String.valueOf(val));
                    break;
                }

                case DeviceProtocol.ENERGY_REMAINS: {
                    float en_val = BytesInterpret.toFloat(actualValues); // LE BE ?
                    energyRemainsView.setText(String.valueOf(en_val));
                    break;
                }

                case DeviceProtocol.ENERGY_CONSUMPTION: {
                    float en_co = BytesInterpret.toFloat(actualValues); // LE BE ?
                    energyConsumptionView.setText(String.valueOf(en_co));
                    break;
                }

                case DeviceProtocol.BUTTERY_VOLTAGE: {
                    float value = BytesInterpret.asFloat(actualValues);
                    batteryVoltageView.setText(String.valueOf(value));
                    break;
                }
            }
    }

    private void refreshRenderedDataGroup(byte[] values) {
        int index_values_start = 2;
        for (int i = 0; i < optimizedRequest.getBufferForRequest().size(); i++) {
            refreshRenderedData(
                    Arrays.copyOfRange(values, index_values_start - 1, index_values_start + optimizedRequest.getBufferForRequest().get(i).size),
                    optimizedRequest.getBufferForRequest().get(i)
            );
            index_values_start += optimizedRequest.getBufferForRequest().get(i).size;
        }
    }

    private final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        Handler handler = new Handler();
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DataManager.InfoFragmentRequests) {
                byte[] value = (byte[]) intent.getSerializableExtra(DeviceProtocol.TX_DATA_DETECTED_EXTRA);
                if (isErrorResponse(value[0])) {
                    Log.e("Monitor", String.format("Command 0x%h raises error 0x%h", extractErrorCommand(value[0]), value[1]));
                } else {
                    refreshRenderedDataGroup(value);

                    // get next value
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            dataPackages.next(DataPackages.InfoFragmentDataPackage);

                            DataManager.InfoFragmentRequests = true;
                            optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.InfoFragmentDataPackage);
                            DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.INFO_FRAGMENT_DATA_PACKAGE);
//                            DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.INFO_FRAGMENT_DATA_PACKAGE);
                        }
                    }, 100);
                }

                DataManager.InfoFragmentRequests = false;
            }
        }
    };
}