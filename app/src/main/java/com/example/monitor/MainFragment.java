package com.example.monitor;

import static com.example.monitor.DataManager.extractErrorCommand;
import static com.example.monitor.DataManager.isErrorResponse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.welie.blessed.BluetoothPeripheral;

import java.util.Arrays;


public class MainFragment extends Fragment {

    View                fragmentAdditionalInfoView;
    AwesomeSpeedometer  speedometer;
    PointerSpeedometer  current;
    TextView            speedNumber;
    TextView            lightState;
    TextView            AbsAsrState;
    TextView            deviceEnabled;
    TextView            additionalInfo;
    ProgressBar         energyRemains;

    BluetoothLayer bluetoothLayer = null;

    DataPackages dataPackages = new DataPackages();  // указатель на текущий элемент интерфейса
    OptimizedRequest optimizedRequest = null;

    private boolean firstStart = true;

    private int selectedAdditional;

    View.OnClickListener fragmentAdditionalOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }
    };

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fragmentAdditionalInfoView = view.findViewById(R.id.additionalInfoText);
        speedometer                = view.findViewById(R.id.speedView);
        current                    = view.findViewById(R.id.currentView);
        speedNumber                = view.findViewById(R.id.speedIndicator);
        lightState                 = view.findViewById(R.id.lightIndicator);
        AbsAsrState                = view.findViewById(R.id.ABSASRIndicator);
        deviceEnabled              = view.findViewById(R.id.lockIndicator);
        additionalInfo             = view.findViewById(R.id.additionalInfoValue);
        energyRemains              = view.findViewById(R.id.energyRemains);

        fragmentAdditionalInfoView.setOnClickListener(fragmentAdditionalOnClickListener);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        DataManager.MainFragmentRequests = false;

        BluetoothPeripheral connectedFromSettingsFragment = ((MainActivity) getActivity()).getConnectedDevice();
        getContext().registerReceiver(txReceiver, new IntentFilter(DeviceProtocol.TX_DATA_DETECTED));
        if (connectedFromSettingsFragment != null) {
            bluetoothLayer = BluetoothLayer.getInstance(getContext());
            bluetoothLayer.setBoundedDevice(connectedFromSettingsFragment.getAddress());

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DataManager.MainFragmentRequests = true;
                    optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.MainFragmentDataPackage);
                    DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.MAIN_FRAGMENT_DATA_PACKAGE);
//                    DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.MAIN_FRAGMENT_DATA_PACKAGE);
                }
            }, 200);

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DataManager.MainFragmentRequests = false;
        getContext().unregisterReceiver(txReceiver);
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.popup_on_main_menu);

        TextView optionalIndicator = getView().findViewById(R.id.additionalInfoText);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        selectedAdditional = item.getItemId();
                        switch (item.getItemId()) {
                            case R.id.generalRunMenuItem:
                                optionalIndicator.setText(R.string.general_run);
                                return true;
                            case R.id.RunMenuItem:
                                optionalIndicator.setText(R.string.run);
                                return true;
                            case R.id.temperatureMenuItem:
                                optionalIndicator.setText(R.string.engine_temperature);
                                return true;

                            case R.id.CANStateMenuItem:
                                optionalIndicator.setText(R.string.can_state);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        popupMenu.show();
    }

    /**
     * Обновляет элемент интерфейса в соответствии с текущим указателем на него,
     *
     * @param actualValues новое значение для элемента интерфейса
     */
    private void refreshRenderedData(byte[] actualValues, DataView currentDataView) {

        Log.e("Monitor", String.format("MainFragment refresh %s", BytesInterpret.dumpBytes(actualValues)));

        switch (currentDataView.address) {
            case DeviceProtocol.SPEED: {
                // set speed value
                float value = BytesInterpret.asFloat(actualValues);
                speedometer.speedTo(value);
                break;
            }

            case DeviceProtocol.CURRENT: {
                // set current value
                float value = BytesInterpret.asFloat(actualValues);
                current.speedTo(value);
                break;
            }

            case DeviceProtocol.MILEAGE: {
                if (selectedAdditional == R.id.RunMenuItem) {
                    float value = BytesInterpret.toFloat(actualValues); // LE BE ?
                    additionalInfo.setText(String.valueOf(value));
                }
                break;
            }

            case DeviceProtocol.COMMON_MILEAGE: {
                if (selectedAdditional == R.id.generalRunMenuItem) {
                    float value = BytesInterpret.toFloat(actualValues); // LE BE ?
                    additionalInfo.setText(String.valueOf(value));
                }
                break;
            }

            case DeviceProtocol.IEC_TEMPERATURE: {
                if (selectedAdditional == R.id.CANStateMenuItem) {
                    float value = BytesInterpret.toFloat(actualValues); // LE BE ?
                    additionalInfo.setText(String.valueOf(value));
                }
                break;
            }

            case DeviceProtocol.STATE_REGISTER_1: {
                // set speed number
                if ((actualValues[0] & DeviceProtocol.FIRST_SPEED) == DeviceProtocol.FIRST_SPEED) {

                    speedNumber.setText("1");

                } else if ((actualValues[0] & DeviceProtocol.SECOND_SPEED) == DeviceProtocol.SECOND_SPEED) {

                    speedNumber.setText("2");

                } else if ((actualValues[0] & DeviceProtocol.THIRD_SPEED) == DeviceProtocol.THIRD_SPEED) {

                    speedNumber.setText("3");

                } else if ((actualValues[1] & DeviceProtocol.BRAKE) == DeviceProtocol.BRAKE) {

                    speedNumber.setText("STOP");

                }

                // set light state
                if ((actualValues[1] & DeviceProtocol.POSITION_LIGHT) == DeviceProtocol.POSITION_LIGHT) {

                    lightState.setText("габариты");

                } else if ((actualValues[1] & DeviceProtocol.HEADLIGHT) == DeviceProtocol.HEADLIGHT) {

                    lightState.setText("фара");

                } else if ((actualValues[2] & DeviceProtocol.DECO_LIGHT) == DeviceProtocol.DECO_LIGHT) {

                    lightState.setText("декоративный");

                }

                // device enabled
                if ((actualValues[0] & DeviceProtocol.DEVICE_ON)  == DeviceProtocol.DEVICE_ON) {

                    deviceEnabled.setText("Device ON");

                } else {

                    deviceEnabled.setText("Device OF");

                }
                break;
            }

            case DeviceProtocol.ENERGY_REMAINS: {
                // battery remains
                float value = BytesInterpret.toFloat(actualValues); // LE BE ?
                energyRemains.setProgress((int) value);
                break;
            }

            case DeviceProtocol.ABS_ASR_MODE: {
                // check if int8 represented in actualValues[1] == 1
                if (BytesInterpret.asOn(actualValues[1])) {
                    AbsAsrState.setText("ABS/ASR");
                }
                break;
            }

            default: {}
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
            if (DataManager.MainFragmentRequests) {
                byte[] value = (byte[]) intent.getSerializableExtra(DeviceProtocol.TX_DATA_DETECTED_EXTRA);
                if (isErrorResponse(value[0])) {

                    Log.e("Monitor", String.format("Command 0x%h raises error 0x%h", extractErrorCommand(value[0]), value[1]));

                } else {

//                    refreshRenderedData(value);
                    refreshRenderedDataGroup(value);

                    // get next value
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            dataPackages.next(DataPackages.MainFragmentDataPackage);
                            DataManager.MainFragmentRequests = true;
                            optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.MainFragmentDataPackage);
                            DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.MAIN_FRAGMENT_DATA_PACKAGE);
//                            DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.MAIN_FRAGMENT_DATA_PACKAGE);
                        }

                    }, 100);
                }

                DataManager.MainFragmentRequests = false;
            }
        }
    };
}