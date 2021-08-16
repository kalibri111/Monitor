package com.example.monitor;

import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.welie.blessed.BluetoothPeripheral;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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

    DataPackages dataPackages = new DataPackages();

    Runnable requestData = null;

    private int selectedAdditional;

    View.OnClickListener fragmentAdditionalOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }
    };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
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


        view.getContext().registerReceiver(txReceiver, new IntentFilter(DeviceInfo.TX_DATA_DETECTED));

//        speedometer.speedTo(15);  // ONLY FOR DEBUG

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothPeripheral connectedFromSettingsFragment = ((MainActivity) getActivity()).getConnectedDevice();

        if (connectedFromSettingsFragment != null) {
            bluetoothLayer = BluetoothLayer.getInstance(getContext());
            bluetoothLayer.setBoundedDevice(connectedFromSettingsFragment.getAddress());

            requestData = new Runnable() {
                @Override
                public void run() {
                    requestDataView();
                }
            };

            requestData.run();
        }
    }

    private void requestDataView() {
        // request current DataView from DataPackages
        DataView currentDataView = dataPackages.getMainFragmentDataView();
        if (currentDataView.table == DeviceInfo.READ_TABLE) {

            switch (currentDataView.size) {
                case 1: {
                    // read byte
                    readCommand(DeviceInfo.READ_BYTE_COMMAND, currentDataView.address);                        }

                case 2: {
                    // read word
                    readCommand(DeviceInfo.READ_WORD_COMMAND, currentDataView.address);
                }

                case 4: {
                    // read dword
                    readCommand(DeviceInfo.READ_DWORD_COMMAND, currentDataView.address);
                }
            }

        } else if (currentDataView.table == DeviceInfo.READ_WRITE_TABLE) {

            switch (currentDataView.size) {
                case 1: {
                    // read byte
                    readCommand(DeviceInfo.READ_READWRITE_BYTE_COMMAND, currentDataView.address);                        }

                case 2: {
                    // read word
                    readCommand(DeviceInfo.READ_READWRITE_WORD_COMMAND, currentDataView.address);
                }

                case 4: {
                    // read dword
                    readCommand(DeviceInfo.READ_READWRITE_DWORD_COMMAND, currentDataView.address);
                }
            }

        }
    }

    private void readCommand(byte command, int address) {
        bluetoothLayer.writeRXCharacteristic(
                SettingsFragment.makeRequestPackage(
                        command,
                        new byte[] {(byte) address}
                )
        );
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

    private void refreshRenderedData(byte[] actualValues) {

        DataView currentDataView = dataPackages.getMainFragmentDataView();

        switch (currentDataView.address) {
            case DeviceInfo.SPEED: {
                // set speed value
                float speedValue = actualValues[0] + (float)actualValues[1] / 10;
                speedometer.speedTo(speedValue);
            }

            case DeviceInfo.CURRENT: {
                // set current value
                float currentValue = actualValues[0] + (float)actualValues[1] / 10;
                speedometer.speedTo(currentValue);
            }

            case DeviceInfo.MILEAGE: {
                if (selectedAdditional == R.id.RunMenuItem) {
                    float val = ByteBuffer.wrap(actualValues).getFloat(); // LE BE ?
                    additionalInfo.setText(String.valueOf(val));
                }
            }

            case DeviceInfo.COMMON_MILEAGE: {
                if (selectedAdditional == R.id.generalRunMenuItem) {
                    float val = ByteBuffer.wrap(actualValues).getFloat(); // LE BE ?
                    additionalInfo.setText(String.valueOf(val));
                }
            }

            case DeviceInfo.IEC_TEMPERATURE: {
                if (selectedAdditional == R.id.CANStateMenuItem) {
                    float val = ByteBuffer.wrap(actualValues).getFloat(); // LE BE ?
                    additionalInfo.setText(String.valueOf(val));
                }
            }

            case DeviceInfo.STATE_REGISTER_1: {
                // set speed number
                if ((actualValues[0] & DeviceInfo.FIRST_SPEED) == DeviceInfo.FIRST_SPEED) {

                    speedNumber.setText("1");

                } else if ((actualValues[0] & DeviceInfo.SECOND_SPEED) == DeviceInfo.SECOND_SPEED) {

                    speedNumber.setText("2");

                } else if ((actualValues[0] & DeviceInfo.THIRD_SPEED) == DeviceInfo.THIRD_SPEED) {

                    speedNumber.setText("3");

                } else if ((actualValues[1] & DeviceInfo.BRAKE) == DeviceInfo.BRAKE) {

                    speedNumber.setText("STOP");

                }

                // set light state
                if ((actualValues[1] & DeviceInfo.POSITION_LIGHT) == DeviceInfo.POSITION_LIGHT) {

                    lightState.setText("габариты");

                } else if ((actualValues[1] & DeviceInfo.HEADLIGHT) == DeviceInfo.HEADLIGHT) {

                    lightState.setText("фара");

                } else if ((actualValues[2] & DeviceInfo.DECO_LIGHT) == DeviceInfo.DECO_LIGHT) {

                    lightState.setText("декоративный");

                }

                // device enabled
                if ((actualValues[0] & DeviceInfo.DEVICE_ON)  == DeviceInfo.DEVICE_ON) {

                    deviceEnabled.setText("Device ON");

                } else {

                    deviceEnabled.setText("Device OF");

                }

            }

            case DeviceInfo.ENERGY_REMAINS: {
                // battery remains
                float en_val = ByteBuffer.wrap(actualValues).getFloat(); // LE BE ?
                energyRemains.setProgress((int) en_val);
            }

            case DeviceInfo.ABS_ASR_MODE: {
                // check if int8 represented in actualValues[0] == 1
                if ((actualValues[0] & 0x01) == 0x01) {
                    AbsAsrState.setText("ABS/ASR");
                }
            }

            default: {}
        }
    }

    private float convertSpeed(byte[] value) {
        int bigger = value[1] & 0xff;
        int lesser = value[2] & 0xff;
        return (float)((bigger << 8) + lesser) / 10;
    }

    private final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        Handler handler = new Handler();
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] value = (byte[]) intent.getSerializableExtra(DeviceInfo.TX_DATA_DETECTED_EXTRA);
            refreshRenderedData(value);
            // get next value
            dataPackages.next(DataPackages.MainFragmentDataPackage);
            handler.postDelayed(requestData, 100);
        }
    };
}