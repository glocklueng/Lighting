package com.example.administrator.lighting.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.manager.BaseActivity;
import com.example.administrator.lighting.util.LogUtil;

public class ControlActivity extends BaseActivity {

    private final static String TAG = ControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        LogUtil.d(TAG, mDeviceName);
        LogUtil.d(TAG, mDeviceAddress);
    }
}
