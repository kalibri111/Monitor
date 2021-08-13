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

//                initial request
            requestData = new Runnable() {
                @Override
                public void run() {
//                        FOR TEST ONLY
                    bluetoothLayer.writeRXCharacteristic(SettingsFragment.makeRequestPackage(DeviceInfo.READ_WORD_COMMAND, new byte[] {0x01}));
//                        bluetoothLayer.writeRXCharacteristic(SettingsFragment.makeRequestPackage(DeviceInfo.READ_INDICATION_DATA_GROUP, new byte[] {}));
                }
            };

            requestData.run();
        }
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
        // set speed number
        if (actualValues[DeviceInfo.FIRST_SPEED] == 1) {
            speedNumber.setText("1");
        } else if (actualValues[DeviceInfo.SECOND_SPEED] == 1) {
            speedNumber.setText("2");
        } else if (actualValues[DeviceInfo.THIRD_SPEED] == 1) {
            speedNumber.setText("3");
        } else if (actualValues[DeviceInfo.BRAKE] == 1) {
            speedNumber.setText("STOP");
        }

        // set speed value
        float speedValue = actualValues[DeviceInfo.SPEED] + (float)actualValues[DeviceInfo.SPEED + 1] / 10;
        speedometer.speedTo(speedValue);

        // set current value
        float currentValue = actualValues[DeviceInfo.CURRENT] + (float)actualValues[DeviceInfo.CURRENT + 1] / 10;
        speedometer.speedTo(currentValue);

        // set light state
        if (actualValues[DeviceInfo.LIGHT] == 1) {
            lightState.setText("Enabled");
        } else {
            lightState.setVisibility(View.INVISIBLE);
        }

        // set abs ars state
        if (actualValues[DeviceInfo.ABS] == 1) {
            AbsAsrState.setText("ABS");
        } else if (actualValues[DeviceInfo.ASR] == 1) {
            AbsAsrState.setText("ASR");
        } else {
            AbsAsrState.setVisibility(View.INVISIBLE);
        }

        // device enabled
        if (actualValues[DeviceInfo.DEVICE_ON] == 1) {
            deviceEnabled.setText("Device ON");
        } else {
            deviceEnabled.setText("Device OF");
        }

        // additional info
        switch (selectedAdditional) {
            case R.id.generalRunMenuItem:{
                additionalInfo.setText(actualValues[DeviceInfo.COMMON_MILEAGE_INDICATION]);
            }
            case R.id.RunMenuItem:{
                additionalInfo.setText(actualValues[DeviceInfo.MILEAGE_INDICATION]);
            }
            case R.id.temperatureMenuItem:{
                additionalInfo.setText(actualValues[DeviceInfo.IEC_TEMPERATURE_INDICATION]);
            }

            case R.id.CANStateMenuItem:{
                additionalInfo.setText(actualValues[DeviceInfo.CAN_STATE_INDICATION]);
            }
            // TODO: default?
        }

        // battery remains
        energyRemains.setProgress(actualValues[DeviceInfo.ENERGY_REMAINS]);
    }

    private final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        Handler handler = new Handler();
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] value = (byte[]) intent.getSerializableExtra(DeviceInfo.TX_DATA_DETECTED_EXTRA);
//            FOR TEST ONLY
            switch (value[0]){
                case DeviceInfo.READ_WORD_COMMAND: {
                    int bigger = value[1] & 0xff;
                    int lesser = value[2] & 0xff;
                    float speed = (float)((bigger << 8) + lesser) / 10;
                    speedometer.speedTo(speed);
                    Log.e("Monitor", String.format("set speed to %f from %h %h", speed, value[1], value[2]));
                }

            }
            handler.postDelayed(requestData, 500);
//            if (value[0] == DeviceInfo.READ_INDICATION_DATA_GROUP) {
//                refreshRenderedData(value);
//                handler.postDelayed(requestData, 100);
//            }
        }
    };
}