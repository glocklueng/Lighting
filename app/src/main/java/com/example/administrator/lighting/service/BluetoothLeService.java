package com.example.administrator.lighting.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.administrator.lighting.util.LogUtil;

import java.util.UUID;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private final static int STATE_DISCONNECTED = 0;
    private final static int STATE_CONNECTING = 1;
    private final static int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED    = "com.example.administrator.lighting.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.administrator.lighting.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_WRITE = "com.example.administrator.lighting.ACTION_DATA_WRITE";

    public static final String FFF0_CHAR  = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static final String FFF1_CHAR  = "0000FFF1-0000-1000-8000-00805F9B34FB";
    public static final String FFF2_CHAR  = "0000FFF2-0000-1000-8000-00805F9B34FB";

    public final static UUID UUID_SERVICE = UUID.fromString(FFF0_CHAR);
    public final static UUID UUID_NOTIFY = UUID.fromString(FFF1_CHAR);
    public final static UUID UUID_WRITE = UUID.fromString(FFF2_CHAR);

    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                LogUtil.d(TAG, "Connected to GATT service.");
                //连接成功后去寻找服务，只有寻找到服务后才能与设备进行通信
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                LogUtil.d(TAG, "Disconnected from GATT service.");
            }
        }

        /**
         * 调用discoverServices()寻找到服务后会调用这里
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(TAG, "start send data");
                sendData(new byte[] { 0x08, 0x07, 0x01, 0x00, 0x00, 0x10, -128 });
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
            }
        }

        /**
         * 调用writeCharacteristic()硬件流真正将数据发出后会调用这里
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(TAG, "send data is succeed!");
                broadcastUpdate(ACTION_DATA_WRITE, characteristic);
            }
        }
    };

    private void sendData(byte[] value) {
        BluetoothGattService sendService = mBluetoothGatt.getService(UUID_SERVICE);
        if (sendService != null) {
            BluetoothGattCharacteristic sendCharacteristic = sendService.getCharacteristic(UUID_WRITE);
            if (sendCharacteristic != null) {
                sendCharacteristic.setValue(value);
                mBluetoothGatt.writeCharacteristic(sendCharacteristic);
            }
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            LogUtil.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            LogUtil.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}
