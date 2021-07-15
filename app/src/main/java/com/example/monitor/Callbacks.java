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
import android.location.LocationManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Handler;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class Callbacks extends AppCompatActivity {
    //******************************************************
    public  static final int    GATT_INTERNAL_ERROR = 0x0081;
    private static final String TAG                 = "Monitor";

    private Handler            bleHandler = new Handler();

    private BluetoothDevice       boundedDevice = null;
    private BluetoothGatt         gatt          = null;
    private BluetoothAdapter      adapter       = null;
    private BluetoothLeScanner    scanner       = null;
    private BluetoothManager   mBluetoothManager = null;
    private LeDeviceListAdapter   devicesList   = new LeDeviceListAdapter();
    private ArrayList<String>     devicesListNames   = new ArrayList<String>();
    private Context               context       = null;
    private Activity              activity      = null;

    private int connectionState                 = STATE_DISCONNECTED;

    private Runnable discoverServicesRunnable = null;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.nordicsemi.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private byte[] txAnswer = null;

    //******************************************************

    public Callbacks(Context appContext, Activity appActivity) {
        context = appContext;
        activity = appActivity;

        initialize();
    }

    //******************************************************

    public ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("Monitor", "onScanResult");

            if (!devicesList.contains(result.getDevice())) {
                devicesList.add(result.getDevice());
                devicesListNames.add(result.getDevice().getName());
            }

            Toast.makeText(context, "We found device " + result.getDevice().getName(), Toast.LENGTH_SHORT).show();
            stopScan();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("Monitor", "onBatchScanResults: " + results.size() + " results");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w("Monitor", "LE Scan Failed: " + errorCode);
        }


    };

    public BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (status == GATT_SUCCESS) {
                stopScan();
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    int bondstate = boundedDevice.getBondState();
                    // Обрабатываем bondState
                    if(bondstate == BluetoothDevice.BOND_NONE || bondstate == BluetoothDevice.BOND_BONDED) {
                        // Подключились к устройству, вызываем discoverServices с задержкой
                        int delayWhenBonded = 0;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            delayWhenBonded = 1000;
                        }
                        final int delay = bondstate == BluetoothDevice.BOND_BONDED ? delayWhenBonded : 0;
                        discoverServicesRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, String.format(Locale.ENGLISH, "discovering services of '%s' with delay of %d ms", boundedDevice.getName(), delay));
                                boolean result = gatt.discoverServices();
                                if (!result) {
                                    Log.e(TAG, "discoverServices failed to start");
                                }
                                discoverServicesRunnable = null;
                            }
                        };
                        bleHandler.postDelayed(discoverServicesRunnable, delay);
                    } else if (bondstate == BluetoothDevice.BOND_BONDING) {
                        // Bonding в процессе, ждем когда закончится
                        Log.i(TAG, "waiting for bonding to complete");
                    }

                    gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "We successfully disconnected on our own request");
                    gatt.close();
                } else { }

                bleHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableTXNotification();
                    }
                }, 100);

                bleHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeRXCharacteristic(new byte[]{0x10, 0x31, 0x32, 0x33, 0x34, 0x35, 0x35});
                    }
                }, 100);

            } else {
                // An error happened
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // Проверяем есть ли ошибки? Если да - отключаемся
            if (status == GATT_INTERNAL_ERROR) {
                Log.e(TAG, "Service discovery failed");
                gatt.disconnect();
                return;
            }

            final List<BluetoothGattService> services = gatt.getServices();

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "Descriptor was written");

            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            txAnswer = characteristic.getValue();
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (getTxAnswer() == null) {
                Toast.makeText(context, "No answer", Toast.LENGTH_SHORT).show();
            } else if (getTxAnswer()[0] == 0x10) {
                Toast.makeText(context, "Password OK", Toast.LENGTH_SHORT).show();
            } else if (getTxAnswer()[0] == 0x06) {
                Toast.makeText(context, "Password WRONG", Toast.LENGTH_SHORT).show();
            } else if (getTxAnswer()[0] == 0x90) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

    };

    //******************************************************

    public void startScan(LocationManager manager) {
        LocationstatusCheck(manager);
        List<ScanFilter> filters = null;
        filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(RX_SERVICE_UUID))
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Log.d("Monitor", "startScan ahead");
        if (scanner != null) {
            Log.d("Monitor", "scan started");
            scanner.startScan(filters, settings, mScanCallback);
        } else {
            Log.d("Monitor", "could not get scanner object");
        }
    }

    public void stopScan() {
        scanner.stopScan(mScanCallback);
    }

    public void LocationstatusCheck(LocationManager manager) {

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(context, activity);
        }
    }

    private void buildAlertMessageNoGps(Context context, Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

    public BluetoothDevice getBoundedDeviceByName(String name) {
        for (BluetoothDevice d : devicesList) {
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

    public boolean connect(final String address) {
        stopScan();
        if (adapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (boundedDevice.getAddress() != null && address.equals(boundedDevice.getAddress())
                && gatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (gatt.connect()) {
                connectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

//        final BluetoothDevice device = adapter.getRemoteDevice(address);
        if (boundedDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        gatt = boundedDevice.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public void enableTXNotification() {
    	/*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
        BluetoothGattService RxService = gatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        gatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);

    }

    public void writeRXCharacteristic(byte[] value) {


        BluetoothGattService RxService = gatt.getService(RX_SERVICE_UUID);
        showMessage("gatt null" + gatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
        boolean status = gatt.writeCharacteristic(RxChar);

        Log.d(TAG, "write TXchar - status=" + status);
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is handling for the notification on TX Character of NUS service
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {

            // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else {

        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public byte[] getTxAnswer() {
        return txAnswer;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) activity.getSystemService(BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        adapter = mBluetoothManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        scanner = adapter.getBluetoothLeScanner();
        return true;
    }
}
