package com.example.monitor;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Handler;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import static com.welie.blessed.BluetoothBytesParser.bytes2String;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.WriteType;

public class BluetoothLayer extends AppCompatActivity {
    //******************************************************
    public  static final int    GATT_INTERNAL_ERROR = 0x0081;
    private static final String TAG                 = "Monitor";



    private BluetoothPeripheral       boundedDevice = null;
    private BluetoothGatt         gatt          = null;
    private BluetoothAdapter      adapter       = null;
    private BluetoothLeScanner    scanner       = null;
    private BluetoothManager   mBluetoothManager = null;
    private LeDeviceListAdapter   devicesList   = new LeDeviceListAdapter();
    private ArrayList<String>     devicesListNames   = new ArrayList<String>();
    private Context               context       = null;
    private Activity              activity      = null;

    private ArrayAdapter<String> namesAdapter = null;

    private int connectionState                 = STATE_DISCONNECTED;

    private BluetoothCentralManager centralManager = null;

    private Runnable discoverServicesRunnable = null;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static BluetoothLayer instance = null;

    //******************************************************

    public BluetoothLayer(Context appContext, Activity appActivity) {
        context = appContext;
        activity = appActivity;
        centralManager = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

    }

    public void setNamesAdapter(ArrayAdapter<String> namesAdapter) {
        this.namesAdapter = namesAdapter;
    }

    //******************************************************

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
            centralManager.stopScan();
            devicesList.add(peripheral);
            devicesListNames.add(peripheral.getName());
            if (namesAdapter != null) {

                namesAdapter.notifyDataSetChanged();

            }
            Log.i(TAG, String.format("Found device %s", peripheral.getName()));

        }
    };

    private final BluetoothPeripheralCallback bluetoothPeripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
            enableTXNotification();
//            writeRXCharacteristic(new byte[]{0x10, '1', '2', '3', '4', '5', '6'});  // password here
        }

        @Override
        public void onNotificationStateUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                if(peripheral.isNotifying(characteristic)) {
                    Log.i(TAG, String.format("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid()));
                } else {
                    Log.i(TAG, String.format("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid()));
                }
            } else {
                Log.e(TAG, String.format("ERROR: Changing notification state failed for %s", characteristic.getUuid()));
            }
        }

        @Override
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status) {

            // Broadcast incoming data
            Intent intent = new Intent(DeviceInfo.TX_DATA_DETECTED);
            intent.putExtra(DeviceInfo.TX_DATA_DETECTED_EXTRA, value);
            context.sendBroadcast(intent);

            Log.e(TAG, String.format("GET TX answer: %h from characteristic %s", value[0], characteristic.getUuid().toString()));
        }

        @Override
        public void onCharacteristicWrite(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Log.i(TAG, String.format("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid()));
            } else {
                Log.i(TAG, String.format("ERROR: Failed writing <%s> to <%s> (%s)", bytes2String(value), characteristic.getUuid(), status));
            }
        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattDescriptor descriptor, @NonNull GattStatus status) {
            super.onDescriptorRead(peripheral, value, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattDescriptor descriptor, @NonNull GattStatus status) {
            super.onDescriptorWrite(peripheral, value, descriptor, status);
        }

        @Override
        public void onConnectionUpdated(@NonNull BluetoothPeripheral peripheral, int interval, int latency, int timeout, @NonNull GattStatus status) {
            super.onConnectionUpdated(peripheral, interval, latency, timeout, status);
        }

    };

    //******************************************************

    public void startScan() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                centralManager.scanForPeripheralsWithServices(new UUID[]{DeviceInfo.RX_SERVICE_UUID});
            }
        }, 1000);

    }

    public void stopScan() {
        centralManager.stopScan();
    }

    public void connect(final BluetoothPeripheral device) {
        centralManager.connectPeripheral(device, bluetoothPeripheralCallback);
    }

    public void disconnect(BluetoothPeripheral device) {
        centralManager.cancelConnection(device);
    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public void enableTXNotification() {
        centralManager.stopScan();

        BluetoothGattCharacteristic txCharacteristic = boundedDevice.getCharacteristic(DeviceInfo.RX_SERVICE_UUID, DeviceInfo.TX_CHAR_UUID);

        if (txCharacteristic != null) {
            boundedDevice.setNotify(txCharacteristic, true);
        } else {
            Log.e(TAG, "TX characteristic error");
        }

    }

    public void writeRXCharacteristic(byte[] value) {
        centralManager.stopScan();

        BluetoothGattCharacteristic rxCharacteristic = boundedDevice.getCharacteristic(DeviceInfo.RX_SERVICE_UUID, DeviceInfo.RX_CHAR_UUID);

        if (rxCharacteristic != null) {
            boundedDevice.writeCharacteristic(rxCharacteristic, value, WriteType.WITH_RESPONSE);
        } else {
            Log.e(TAG, "RX characteristic error");
        }

    }

    public void LocationstatusCheck(LocationManager manager) {

        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            buildAlertMessageNoGps(context, activity);
        }
    }

    //******************************************************

    private void buildAlertMessageNoGps(Context context, Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(enableLocationIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public ArrayList<String> getDevicesList() {
        return devicesListNames;
    }

    public BluetoothPeripheral getBoundedDeviceByName(String name) {
        for (BluetoothPeripheral d : devicesList) {
            if (d.getName().equals(name)) {
                boundedDevice = d;
                return d;
            }
        }

        return null;
    }

    public void clearDevicesList() {
        devicesList.clear();
    }


    public static synchronized BluetoothLayer getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothLayer(context.getApplicationContext());
        }
        return instance;
    }

    private BluetoothLayer(Context context) {
        this.context = context;

        // Create BluetoothCentral
        centralManager = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler());

        // Scan for peripherals with a certain service UUIDs
        centralManager.startPairingPopupHack();
//        startScan();
    }

}
