package com.example.bluetooth.le;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
//import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ListOfScansActivity extends AppCompatActivity {


    public BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add(new CustomBluetoothDeviceWrapper(device,rssi));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    public BleScanServices m_bleScanServices;
    public List<CustomBluetoothDeviceWrapper> devices;
    CustomListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckBleHardware();

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 2);
        // This will pop up the first time the app is run, to give the app permissions not already given.
        m_bleScanServices = new BleScanServices(this);
        m_bleScanServices.checkBluetoothEnabled(this);

        devices = new ArrayList<>();

        setContentView(R.layout.activity_list_of_scans); // Set the content to a layout with list view
        ListView lv = findViewById(R.id.myList); // gets @android:id/list in said activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Intent intent = new Intent(ListOfScansActivity.this, HM10CommunicationActivity.class);
                intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, devices.get(i).getName()); // locally have the position
                intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, devices.get(i).getAddress()); // locally have the position
                // but use the global variable of the array used for the adapter
                startActivity(intent);

            }
        }); // AppCompat needs listview itself to setOnItemClickListener, with the class as a context.
        adapter = new CustomListViewAdapter(this, R.layout.row, devices);
        // to TextView, not sure why context: this is needed
        lv.setAdapter(adapter); // set the list view to inflate with the adapter.


        Toolbar toolbar = findViewById(R.id.app_bar_list_of_scans); // declare the toolbar.
        setSupportActionBar(toolbar); // Make toolbar visible on activity. Relative layout.
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu); // Fills three dots with items.
        return true;
    }

    private void CheckBleHardware() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    public void refresh_Button_Click(View v) {
       BleScanServices.scanForDevices(true,mLeScanCallback);
    }
}
