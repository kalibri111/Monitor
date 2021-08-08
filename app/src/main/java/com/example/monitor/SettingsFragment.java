package com.example.monitor;

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
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.welie.blessed.BluetoothPeripheral;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    LocationManager manager;

    private final Handler handler = new Handler();

    private static final String TAG = "Monitor";

    private Thread dataPipeServiceThread;

    private BluetoothPeripheral connectedDevice = null;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int ACCESS_LOCATION_REQUEST = 2;

    private static boolean isDisconnectButtonEnable = false;

    private static final int PASSWORD_LENGTH = 6;

    ArrayAdapter<String> names = null;



    private BluetoothLayer bluetoothLayer;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        view.getContext().registerReceiver(txReceiver, new IntentFilter(DeviceInfo.TX_DATA_DETECTED));


        ListView devices                        = view.findViewById(R.id.devicesList);
        Button disconnectButton                 = view.findViewById(R.id.disconnectButton);
        Button scanButton                       = view.findViewById(R.id.scanButton);
        TextView maxForwardAccelerationCurrent  = view.findViewById(R.id.maxAccCurrentEdit);
        TextView maxBrakeCurrent                = view.findViewById(R.id.maxBrakeCurrentEdit);
        TextView maxForwardSpeed                = view.findViewById(R.id.maxForwardSpeedEdit);
        TextView maxBackwardSpeed               = view.findViewById(R.id.maxBackwardSpeedEdit);
        TextView maxAccForward                  = view.findViewById(R.id.maxAccForwardEdit);
        TextView maxAccBackward                 = view.findViewById(R.id.maxAccBackwardEdit);
        SwitchCompat engineBrake                = view.findViewById(R.id.engineBrake);
        SwitchCompat lightAuto                  = view.findViewById(R.id.autoLight);
        SwitchCompat lightMode                  = view.findViewById(R.id.nightLight);

        // input data listeners:

        maxForwardAccelerationCurrent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
            }
        });

        maxBrakeCurrent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
            }
        });

        maxForwardSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
            }
        });

        maxBackwardSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
            }
        });

        maxAccForward.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
            }
        });

        maxAccBackward.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (bluetoothLayer != null) {

                }
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

        engineBrake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        });

        lightAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        });

        lightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(txReceiver);
    }

    /*
    * check if given response provides error code
    * */
    public static boolean isErrorResponse(byte response) {
        return response < 0;
    }

    /*
    * extract command gave error from error answer
    * */
    private static byte extractErrorCommand(byte response) {
        return (byte)(response & 0x7f);
    }

    /*
     * big switch of incoming information
     * */
    public final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] value = (byte[]) intent.getSerializableExtra(DeviceInfo.TX_DATA_DETECTED_EXTRA);

            if (isErrorResponse(value[0])) {
                Log.e(TAG, String.format("Command 0x%h raises error 0x%h", extractErrorCommand(value[0]), value[1]));

                switch (value[1]) {
                    case DeviceInfo.WRONG_PASSWORD_ERROR: {
                        Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show();
                        break;
                    }

                }

            } else {
                Log.e(TAG, String.format("Got valid answer 0x%h from device", value[0]));

                switch (value[0]) {
                    case DeviceInfo.PASS_PASSWORD_COMMAND: {
                        Toast.makeText(context, "Password OK", Toast.LENGTH_SHORT).show();

                        ((MainActivity)getActivity()).setConnectedDevice(connectedDevice);

                        break;
                    }

                    case DeviceInfo.NEW_PASSWORD_COMMAND: {
                        Toast.makeText(context, "Successfully set new password", Toast.LENGTH_SHORT).show();

//                        bluetoothLayer.writeRXCharacteristic(makeRequestPackage(DeviceInfo.PASS_PASSWORD_COMMAND, "111111".getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }

        }
    };

    public static byte[] makeRequestPackage(byte command, byte[] commandBody) {
        byte[] result = new byte[1 + commandBody.length];
        result[0] = command;
        System.arraycopy(commandBody, 0, result, 1, commandBody.length);
        return result;
    }

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
                            bluetoothLayer.writeRXCharacteristic(
                                    makeRequestPackage(
                                            DeviceInfo.PASS_PASSWORD_COMMAND,
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