package com.example.monitor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

import com.welie.blessed.BluetoothCentralManager;

public class Callbacks extends AppCompatActivity {
    public static final int GATT_INTERNAL_ERROR = 0x0081;

    private static final String TAG = "Monitor";

    private Handler            mHandler   = null;
    private Handler            bleHandler = new Handler();

    private BluetoothAdapter   adapter = null;
    private BluetoothLeScanner scanner = null;
    private BluetoothDevice    device  = null;

    private BluetoothCentralManager centralManager = null;

    private Runnable discoverServicesRunnable = null;

    public Callbacks(Handler handler, BluetoothLeScanner btscanner) {
        mHandler = handler;
        scanner = btscanner;
    }

    public ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("Monitor", "onScanResult");
            BluetoothDevice ptr = result.getDevice();

//            gatt = result.getDevice().connectGatt(MainActivity.this, true, bluetoothGattCallback);
            mHandler.sendMessage(Message.obtain(mHandler, 0, ptr));
            Toast.makeText(Callbacks.this, "We found device " + result.getDevice().getName(), Toast.LENGTH_SHORT).show();
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

    public void startScan() {
        LocationstatusCheck();
        List<ScanFilter> filters = null;
        filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(DeviceInfo.deviceUUID)
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

    public void LocationstatusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableLocationIntent, 1);
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

    public BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (status == GATT_SUCCESS) {
                stopScan();
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    int bondstate = device.getBondState();
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
                                Log.d("Monitor", String.format(Locale.ENGLISH, "discovering services of '%s' with delay of %d ms", device.getName(), delay));
                                boolean result = gatt.discoverServices();
                                if (!result) {
                                    Log.e("Monitor", "discoverServices failed to start");
                                }
                                discoverServicesRunnable = null;
                            }
                        };
                        bleHandler.postDelayed(discoverServicesRunnable, delay);
                    } else if (bondstate == BluetoothDevice.BOND_BONDING) {
                        // Bonding в процессе, ждем когда закончится
                        Log.i("Monitor", "waiting for bonding to complete");
                    }

                    gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("Monitor", "We successfully disconnected on our own request");
                    gatt.close();
                } else { }
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

        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "Characteristic written");
        }

    };
}
