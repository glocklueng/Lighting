package com.example.administrator.lighting.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.manager.BaseActivity;
import com.example.administrator.lighting.service.BluetoothLeService;
import com.example.administrator.lighting.util.LogUtil;

public class ControlActivity extends AppCompatActivity {

    private final static String TAG = ControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;

    /**
     * manage Service lifecycle
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                LogUtil.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            LogUtil.d(TAG, "device "+ mDeviceAddress + " with service is connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.d(TAG, "service is disconnected!");
            mBluetoothLeService = null;
        }
    };

    /**
     * Handles various events fired by the Service
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtil.d(TAG, action + " received by mGattUpdateReceiver!");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                LogUtil.d(TAG, "action gatt is connected!");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                LogUtil.d(TAG, "action gatt is disconnected!");
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
                LogUtil.d(TAG, "action gatt services is discovered!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        //取出intent中数据
        final Intent intent = getIntent();
        String deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        LogUtil.d(TAG, deviceName);
        LogUtil.d(TAG, mDeviceAddress);
        TextView tvDeviceName = (TextView) findViewById(R.id.id_card_name);
        TextView tvDeviceAddress = (TextView) findViewById(R.id.id_card_address);
        tvDeviceName.setText("当前设备名称：" + deviceName);
        tvDeviceAddress.setText("当前设备地址：" + mDeviceAddress);
        //绑定BluetoothLeService
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mBluetoothLeService.disconnect();
        return super.onKeyDown(keyCode, event);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }
}
