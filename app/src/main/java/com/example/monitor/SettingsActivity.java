package com.example.monitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends AppCompatActivity {
    private BluetoothAdapter   adapter = null;
    private BluetoothLeScanner scanner = null;
    private BluetoothDevice    device  = null;
    private BluetoothGatt      gatt    = null;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            device = (BluetoothDevice) msg.obj;
            if (device != null) {
                Log.i("Monitor", device.getName());
            } else {
                Log.i("Monitor", "Devise still null");
            }
        }
    };

    private Callbacks callbacks = new Callbacks(mHandler, scanner);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();

        if (adapter == null || !adapter.isEnabled()) {
            // bluetooth le is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
        }

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.startScan();
                gatt = device.connectGatt(SettingsActivity.this, false, callbacks.bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
        });


    }



}
