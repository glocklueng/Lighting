package com.example.administrator.lighting.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.adapter.LeDeviceListAdapter;
import com.example.administrator.lighting.manager.BaseActivity;
import com.example.administrator.lighting.util.LogUtil;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 1000; //1秒后停止查找蓝牙设备

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler;
    private boolean mScanning;

    /**
     * 扫描到BLE设备后回调这个类
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogUtil.d(TAG, device.getName() + "; " + device.getAddress() + "; " + rssi + "dBm");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        initRecycler();
        initBluetooth();

        findViewById(R.id.id_btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

        /**
         * 点击item后进入相应设备的ControlActivity
         */
        mLeDeviceListAdapter.setOnItemClickListener(new LeDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LogUtil.d(TAG, "click item " + position);
                BluetoothDevice bluetoothDevice = mLeDeviceListAdapter.getDevice(position);
                LogUtil.d(TAG, bluetoothDevice.getName());
                LogUtil.d(TAG, bluetoothDevice.getAddress());
                Intent intent = new Intent();
                intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME, bluetoothDevice.getName());
                intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS, bluetoothDevice.getAddress());
                intent.setClass(MainActivity.this, ControlActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void initBluetooth() {
        //检查是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        //获取BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //检查设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initRecycler() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.id_recycler);
        recyclerView.setHasFixedSize(true); //设置固定大小
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        recyclerView.setAdapter(mLeDeviceListAdapter);
    }

    /**
     * 扫描BLE设备
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    LogUtil.d(TAG, "stop scan bluetooth device");
                    if (mLeDeviceListAdapter.getItemCount() == 0) {
                        Toast.makeText(MainActivity.this, "未发现可用的蓝牙设备", Toast.LENGTH_SHORT).show();
                    }
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            LogUtil.d(TAG, "start scan bluetooth device");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            LogUtil.d(TAG, "stop scan bluetooth device");
        }
    }
}
