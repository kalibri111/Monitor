package com.example.monitor;

import static com.example.monitor.DataManager.extractErrorCommand;
import static com.example.monitor.DataManager.isErrorResponse;
import static com.example.monitor.DataManager.makeRequestPackage;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.welie.blessed.BluetoothPeripheral;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SettingsFragment extends Fragment {
    LocationManager manager;

    private final Handler handler = new Handler();

    private static final String TAG = "Monitor";

    private BluetoothPeripheral connectedDevice = null;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int ACCESS_LOCATION_REQUEST = 2;

    private static boolean isDisconnectButtonEnable = false;

    private static final int PASSWORD_LENGTH = 6;

    private DataPackages dataPackages = new DataPackages();
    OptimizedRequest optimizedRequest = null;

    ArrayAdapter<String> names = null;

    ListView        devices;
    Button          disconnectButton;
    Button          scanButton;
    Button          refreshButton;
    EditText        maxForwardAccelerationCurrent;
    EditText        maxBrakeCurrent;
    EditText        maxForwardSpeed;
    EditText        maxBackwardSpeed;
    EditText        maxAccForward;
    EditText        maxAccBackward;
    EditText        maxMomentAccForward;
    EditText        maxMomentAccBackward;
    SwitchCompat    engineBrake;
    SwitchCompat    lightAuto;
    SwitchCompat    engineSpeedLimitAutoBrake;
    EditText        lightLevel;

    private boolean is_engineBrakeClicked = false;
    private boolean is_lightAutoClicked = false;
    private boolean is_engineSpeedLimitAutoBrakeClicked = false;


    private BluetoothLayer bluetoothLayer;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

//        view.getContext().registerReceiver(txReceiver, new IntentFilter(DeviceProtocol.TX_DATA_DETECTED));


        devices                        = view.findViewById(R.id.devicesList);
        disconnectButton               = view.findViewById(R.id.disconnectButton);
        scanButton                     = view.findViewById(R.id.scanButton);
        maxForwardAccelerationCurrent  = view.findViewById(R.id.maxAccCurrentEdit);
        maxBrakeCurrent                = view.findViewById(R.id.maxBrakeCurrentEdit);
        maxForwardSpeed                = view.findViewById(R.id.maxForwardSpeedEdit);
        maxBackwardSpeed               = view.findViewById(R.id.maxBackwardSpeedEdit);
        maxAccForward                  = view.findViewById(R.id.maxAccForwardEdit);
        maxAccBackward                 = view.findViewById(R.id.maxAccBackwardEdit);
        engineBrake                    = view.findViewById(R.id.engineBrake);
        lightAuto                      = view.findViewById(R.id.autoLight);
        lightLevel                     = view.findViewById(R.id.lightLevel);
        engineSpeedLimitAutoBrake      = view.findViewById(R.id.engineAutoSpeedLimitBrake);
        maxMomentAccForward            = view.findViewById(R.id.maxMomentAccForwardEdit);
        maxMomentAccBackward           = view.findViewById(R.id.maxMomentAccBackwardEdit);
        refreshButton                  = view.findViewById(R.id.refreshButton);

        maxMomentAccForward.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxMomentAccForward,
                        DeviceProtocol.MAX_FORWARD_MOMENT_ACCELERATION
                );
            }
        });

        maxMomentAccBackward.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxMomentAccBackward,
                        DeviceProtocol.MAX_BACKWARD_MOMENT_ACCELERATION
                );
            }
        });

        // input data listeners:
        maxForwardAccelerationCurrent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxForwardAccelerationCurrent,
                        DeviceProtocol.MAX_SPEED_UP_CURRENT
                );
            }
        });

        maxBrakeCurrent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxBrakeCurrent,
                        DeviceProtocol.MAX_SPEED_DOWN_CURRENT
                );
            }
        });

        maxForwardSpeed.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxForwardSpeed,
                        DeviceProtocol.MAX_FORWARD_SPEED
                );
            }
        });

        maxBackwardSpeed.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxBackwardSpeed,
                        DeviceProtocol.MAX_BACKWARD_SPEED
                );
            }
        });

        maxAccForward.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxAccForward,
                        DeviceProtocol.MAX_FORWARD_SPEED_ACCELERATION
                );
            }
        });


        maxAccBackward.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return processFloatToEnterKey(
                        event,
                        keyCode,
                        maxAccBackward,
                        DeviceProtocol.MAX_BACKWARD_SPEED_ACCELERATION
                );
            }
        });

        lightLevel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    int value = Integer.parseInt(lightLevel.getText().toString());
                    processByte(DeviceProtocol.LIGHT_LEVEL, (byte)value);
                    return true;
                }
                return false;
            }
        });

        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothLayer.stopScan();
                String itemClicked = (String) parent.getItemAtPosition(position);

                connectedDevice = bluetoothLayer.getBoundedDeviceByName(itemClicked);

                if (connectedDevice != null) {
                    bluetoothLayer.connect(connectedDevice);
                    disconnectButton.setEnabled(true);
                    isDisconnectButtonEnable = true;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buildPasswordAlertDialog();
                        }
                    }, 100);
                } else {
                    Toast.makeText(getContext(), "Выбранное устройство не существует", Toast.LENGTH_SHORT).show();
                }
            }
        });

        engineBrake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engineBrake.isChecked()) {
                    processByte(DeviceProtocol.ENGINE_BRAKE_PROHIBITED, DeviceProtocol.MODE_ON);
                } else {
                    processByte(DeviceProtocol.ENGINE_BRAKE_PROHIBITED, DeviceProtocol.MODE_OF);
                }
            }
        });

        engineSpeedLimitAutoBrake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engineSpeedLimitAutoBrake.isChecked()) {
                    processByte(DeviceProtocol.ENGINE_SPEED_LIMIT_BRAKE_MODE, DeviceProtocol.MODE_ON);
                } else {
                    processByte(DeviceProtocol.ENGINE_SPEED_LIMIT_BRAKE_MODE, DeviceProtocol.MODE_OF);
                }
            }
        });

        lightAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lightAuto.isChecked()) {
                    processByte(DeviceProtocol.LIGHT_AUTO_MODE, DeviceProtocol.MODE_ON);
                } else {
                    processByte(DeviceProtocol.LIGHT_AUTO_MODE, DeviceProtocol.MODE_OF);
                }
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBluetoothGPS();

                names.clear();
                names.notifyDataSetChanged();
                bluetoothLayer.startScan();

                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothLayer.stopScan();
                    }
                }, 5000);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataPackages.isEnd()) {
                    dataPackages.refresh();
                    DataManager.SettingsFragmentRequests = true;
                    optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.SettingsFragmentDataPackage);
                    DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
//                    DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
                }
            }
        });



        disconnectButton.setEnabled(isDisconnectButtonEnable);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedDevice != null) {
                    bluetoothLayer.disconnect(connectedDevice);
                    Toast.makeText(getContext(), String.format("Device %s disconnected", connectedDevice.getName()), Toast.LENGTH_SHORT).show();
                }
                disconnectButton.setEnabled(false);
                isDisconnectButtonEnable = false;
                DataManager.SettingsFragmentRequests = false;
                dataPackages.refresh();
            }
        });

        return view;
    }

    private boolean processFloatToEnterKey(KeyEvent event, int keyCode, EditText editText, int command) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            float value = Float.parseFloat(editText.getText().toString());

            byte[] to_broadcast = new byte[5];
            to_broadcast[0] = (byte) command;
            System.arraycopy(ByteBuffer.allocate(4).putFloat(value).array(), 0, to_broadcast, 1, 4);
            bluetoothLayer.writeRXCharacteristic(makeRequestPackage(DeviceProtocol.WRITE_READWRITE_DWORD_COMMAND, to_broadcast));
            return true;
        }
        return false;
    }

    private void processByte(int command, int value) {
        byte[] to_broadcast = new byte[2];
        to_broadcast[0] = (byte) command;
        to_broadcast[1] = (byte) value;
        bluetoothLayer.writeRXCharacteristic(makeRequestPackage(DeviceProtocol.WRITE_READWRITE_BYTE_COMMAND, to_broadcast));
    }

    @Override
    public void onStart() {
        super.onStart();

        DataManager.SettingsFragmentRequests = false;

        checkBluetoothGPS();

        getContext().registerReceiver(txReceiver, new IntentFilter(DeviceProtocol.TX_DATA_DETECTED));

        if (((MainActivity)getActivity()).getConnectedDevice() != null) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!DataManager.SettingsFragmentRequests) {
                        DataManager.SettingsFragmentRequests = true;
                        optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.SettingsFragmentDataPackage);
                        DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
//                        DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
                    }
                }
            }, 300);

        }
    }


    @Override
    public void onResume() {
        super.onResume();

        checkBluetoothGPS();
    }

    private void checkBluetoothGPS() {
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            if (!isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                checkPermissions();
            }
        } else {
            Log.e(TAG, "This device has no Bluetooth hardware");
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        DataManager.SettingsFragmentRequests = false;
        getContext().unregisterReceiver(txReceiver);
    }

    private void refreshRenderedData(byte[] actualValues, DataView currentDataView) {
//        DataView currentDataView = dataPackages.getSettingsFragmentDataView();

        Log.e("Monitor", String.format("SettingsFragment refresh %s", BytesInterpret.dumpBytes(actualValues)));
        switch (currentDataView.address) {
            case DeviceProtocol.MAX_SPEED_UP_CURRENT: {
                float value = BytesInterpret.toFloat(actualValues);
                maxForwardAccelerationCurrent.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_SPEED_DOWN_CURRENT: {
                float value = BytesInterpret.toFloat(actualValues);
                maxBrakeCurrent.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.ENGINE_SPEED_LIMIT_BRAKE_MODE: {
                engineSpeedLimitAutoBrake.setChecked(BytesInterpret.asOn(actualValues[1]));
                break;
            }
//            case DeviceProtocol.STATE_REGISTER_1: {
//
//            }
            case DeviceProtocol.MAX_FORWARD_SPEED: {
                float value = BytesInterpret.toFloat(actualValues);
                maxForwardSpeed.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_BACKWARD_SPEED: {
                float value = BytesInterpret.toFloat(actualValues);
                maxBackwardSpeed.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_FORWARD_SPEED_ACCELERATION: {
                float value = BytesInterpret.toFloat(actualValues);
                maxAccForward.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_BACKWARD_SPEED_ACCELERATION: {
                float value = BytesInterpret.toFloat(actualValues);
                maxAccBackward.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_FORWARD_MOMENT_ACCELERATION: {
                float value = BytesInterpret.toFloat(actualValues);
                maxMomentAccForward.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.MAX_BACKWARD_MOMENT_ACCELERATION: {
                float value = BytesInterpret.toFloat(actualValues);
                maxMomentAccBackward.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.LIGHT_AUTO_MODE: {
                lightAuto.setChecked(BytesInterpret.asOn(actualValues[1]));
                break;
            }
            case DeviceProtocol.LIGHT_LEVEL: {
                int value = BytesInterpret.toInt8(actualValues[1]);
                lightLevel.setText(String.valueOf(value));
                break;
            }
            case DeviceProtocol.ENGINE_BRAKE_PROHIBITED:{
                engineBrake.setChecked(BytesInterpret.asOn(actualValues[1]));
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

    /*
     * big switch of incoming information
     * */
    public final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        Handler handler = new Handler();
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DataManager.SettingsFragmentRequests) {
                byte[] value = (byte[]) intent.getSerializableExtra(DeviceProtocol.TX_DATA_DETECTED_EXTRA);

                Log.e(TAG, String.format("SettingsFormat receiver got %s", BytesInterpret.dumpBytes(value)));

                if (isErrorResponse(value[0])) {
                    Log.e(TAG, String.format("Command 0x%h raises error 0x%h", extractErrorCommand(value[0]), value[1]));

                    switch (value[1]) {
                        case DeviceProtocol.WRONG_PASSWORD_ERROR: {
                            Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show();
                            break;
                        }

                    }

                } else {
                    Log.e(TAG, String.format("Got valid answer 0x%h from device", value[0]));

                    switch (value[0]) {
                        case DeviceProtocol.PASS_PASSWORD_COMMAND: {

                            Toast.makeText(context, "Password OK", Toast.LENGTH_SHORT).show();

                            ((MainActivity) getActivity()).setConnectedDevice(connectedDevice);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DataManager.SettingsFragmentRequests = true;
                                    optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.SettingsFragmentDataPackage);
                                    DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
//                                    DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
                                }
                            }, 100);

                            break;
                        }

                        default: {
                            if (value.length > 1) {
                                refreshRenderedDataGroup(value);
//                                refreshRenderedData(value);

                                if (!dataPackages.isEnd()) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            optimizedRequest = new OptimizedRequest(dataPackages, DataPackages.SettingsFragmentDataPackage);
                                            DataManager.requestOptimized(optimizedRequest, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
//                                            dataPackages.next(DataPackages.SettingsFragmentDataPackage);
//                                            DataManager.requestDataView(dataPackages, bluetoothLayer, DataPackageType.SETTINGS_FRAGMENT_DATA_PACKAGE);
                                            DataManager.SettingsFragmentRequests = true;
                                        }
                                    }, 100);
                                }
                            }
                            break;
                        }
                    }
                }

                DataManager.SettingsFragmentRequests = false;
            }

        }
    };

    private void buildPasswordAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("PASSWORD");
        alertDialog.setMessage("Enter Password");

        EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(PASSWORD_LENGTH)});
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() == PASSWORD_LENGTH) {
                            DataManager.SettingsFragmentRequests = true;
                            bluetoothLayer.writeRXCharacteristic(
                                    makeRequestPackage(
                                            DeviceProtocol.PASS_PASSWORD_COMMAND,
                                            input.getText().toString().getBytes(StandardCharsets.UTF_8)
                                    )
                            );
                        } else {
                            Toast.makeText(getContext(), String.format("Пароль должен содержать в точности %d цифр", PASSWORD_LENGTH), Toast.LENGTH_SHORT).show();
                        }


                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                ActivityCompat.requestPermissions(getActivity(), missingPermissions, ACCESS_LOCATION_REQUEST);
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getContext().getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getContext().getApplicationInfo().targetSdkVersion;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q)
            return new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
        else return new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work
        if (checkLocationServices()) {
            initBluetoothHandler();

            ListView devices = getView().findViewById(R.id.devicesList);

            names = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    bluetoothLayer.getDevicesList()
            );
            devices.setAdapter(names);


            bluetoothLayer.setNamesAdapter(names);
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if all permission were granted
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            permissionsGranted();
        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Location permission is required for scanning Bluetooth peripherals")
                    .setMessage("Please grant permissions")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            checkPermissions();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void initBluetoothHandler()
    {
        bluetoothLayer = BluetoothLayer.getInstance(getContext().getApplicationContext());
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }


}