package com.example.administrator.lighting.activity;

import android.os.Bundle;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.manager.BaseActivity;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
