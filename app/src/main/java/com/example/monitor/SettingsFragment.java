package com.example.monitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    LocationManager manager;

    private final Handler handler = new Handler();


    private BluetoothAdapter   adapter = null;
    private BluetoothLeScanner scanner = null;
    private BluetoothDevice    device  = null;
    private BluetoothGatt      gatt    = null;

    private LeDeviceListAdapter devicesList = null;

    ArrayAdapter<String> names = null;



    private Callbacks callbacks;

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


        callbacks = new Callbacks(getContext(), getActivity());
        manager   = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        ListView devices = view.findViewById(R.id.devicesList);

        names = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, callbacks.getDevicesList());
        devices.setAdapter(names);

        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemClicked = (String) parent.getItemAtPosition(position);

                device = callbacks.getBoundedDeviceByName(itemClicked);

                if (!callbacks.connect(device.getAddress())) {

                    Toast.makeText(getContext(), "Device connection failed", Toast.LENGTH_SHORT).show();

//                    final Handler handler = new Handler();
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            callbacks.enableTXNotification();
//                        }
//                    }, 100);
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            callbacks.writeRXCharacteristic(new byte[]{0x10, 0x31, 0x32, 0x33, 0x34, 0x35, 0x35});
//                        }
//                    }, 100);




                    // experimental check password



//                    final Handler handler = new Handler();


                }
            }
        });



        if (adapter == null || !adapter.isEnabled()) {
            // bluetooth le is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        Button scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.startScan(manager);


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        names.notifyDataSetChanged();
                    }
                }, 100);

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}