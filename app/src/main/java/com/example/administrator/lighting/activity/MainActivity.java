package com.example.administrator.lighting.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.manager.BaseActivity;
import com.example.administrator.lighting.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; //10秒后停止查找蓝牙设备

    private List<BleDevice> mBleDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    //Recycler
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecycler();
        initBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recycler);
        mRecyclerView.setHasFixedSize(true); //设置固定大小
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerView.setLayoutManager(recyclerLayoutManager);
        mRecyclerAdapter = new LeRecyclerAdapter(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    private void scanLeDevice(final boolean enable) {

    }

    class LeRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

        private LayoutInflater inflater;

        public LeRecyclerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            mBleDeviceList = new ArrayList<BleDevice>();
            mBleDeviceList.add(0, new BleDevice("BLE1", "0001"));
            mBleDeviceList.add(1, new BleDevice("BLE2", "0002"));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.activity_main_recycler_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BleDevice bleDevice = mBleDeviceList.get(position);
            holder.itemTvName.setText(bleDevice.getName());
            holder.itemTvAddress.setText(bleDevice.getAddress());
        }

        @Override
        public int getItemCount() {
            return mBleDeviceList.size();
        }
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_main_recycler_item, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.itemTvName.setText(deviceName);
            } else {
                viewHolder.itemTvName.setText("未知设备");
            }
            viewHolder.itemTvAddress.setText(device.getAddress());
            return view;
        }
    }

    public class BleDevice {
        private String mName;
        private String mAddress;

        public BleDevice(String name, String address) {
            mName = name;
            mAddress = address;
        }

        public String getName() {
            return mName;
        }

        public String getAddress() {
            return mAddress;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView itemCv;
        public TextView itemTvName;
        public TextView itemTvAddress;

        public ViewHolder(View view) {
            super(view);
            itemCv = (CardView) view.findViewById(R.id.id_card);
            itemTvName = (TextView) view.findViewById(R.id.id_card_name);
            itemTvAddress = (TextView) view.findViewById(R.id.id_card_address);
        }
    }
}
