package com.example.monitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class DataPipeService extends Service {

    public DataPipeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(){
            @Override
            public void run() {
                BluetoothLayer bluetoothLayer = BluetoothLayer.getInstance(DataPipeService.this);
                bluetoothLayer.setBoundedDevice(intent.getStringExtra("deviceMAC"));
                while (true) {
                    bluetoothLayer.writeRXCharacteristic(DataManager.makeRequestPackage(DeviceProtocol.READ_WORD_COMMAND, new byte[] {0x01}));

                }
            }
        }.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(txReceiver);
    }

    public final BroadcastReceiver txReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "пиписька", Toast.LENGTH_SHORT).show();
        }
    };
}