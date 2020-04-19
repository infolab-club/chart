package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import club.infolab.isc.bluetooth.AdapterDev;
import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;

public class BluetoothActivity extends AppCompatActivity
        implements AdapterDev.OnTestListener, BluetoothCallback {
    private BluetoothController bluetoothController;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    private ArrayList<String> devicesName = new ArrayList<>();
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private int indexBluetoothDevice = -1;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button buttonConnect;
    private Button buttonSkip;
    // private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        initializeActivity();
        searchPairedDevices();
    }

    private void initializeActivity() {
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterDev(this, devicesName, this);
        recyclerView.setAdapter(adapter);

        buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(onClickConnect);
        buttonSkip = findViewById(R.id.buttonSkip);
        buttonSkip.setOnClickListener(onClickSkip);

        // buttonSearch = findViewById(R.id.scanBut);
        // buttonSearch.setOnClickListener(onClickSearch);
    }

    final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() == null) {
                    setData(device.getAddress());
                }
                else {
                    setData(device.getName());
                }
                bluetoothDevices.add(device);
            }
        }
    };

    private void searchPairedDevices() {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                setData(device.getName());
                bluetoothDevices.add(device);
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);
    }

    public void setData(String deviceName) {
        devicesName.add(deviceName);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceClick(int position) {
        indexBluetoothDevice = position;

        int countViews = recyclerView.getChildCount();
        for (int i = 0; i < countViews; i++) {
            View view = recyclerView.getChildAt(i);
            TextView textDeviceName = view.findViewById(R.id.textDeviceName);
            if (i == indexBluetoothDevice) {
                textDeviceName.setTextColor(getResources().getColor(R.color.dark_ocean));
                textDeviceName.setTypeface(null, Typeface.BOLD);
            }
            else {
                textDeviceName.setTextColor(getResources().getColor(R.color.dark_blue));
                textDeviceName.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void runBluetoothTest() {
        bluetoothController = new BluetoothController(this);
        BluetoothDevice device = bluetoothDevices.get(indexBluetoothDevice);
        device.createBond();
        bluetoothController.connectToDevice(device);
        BluetoothController.isBluetoothRun = true;
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private View.OnClickListener onClickConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (indexBluetoothDevice != -1) {
                runBluetoothTest();
                goToMainActivity();
            }
        }
    };

    private View.OnClickListener onClickSkip = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToMainActivity();
        }
    };

    private View.OnClickListener onClickSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            devicesName.clear();
            searchPairedDevices();
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    public void getInputData(String data) {
//        Toast.makeText(BluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
    }
}