package com.example.administrator.lighting.manager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.lighting.util.LogUtil;

/**
 * 为了让ActivityManager发挥作用，每个Activity都应该继承这个类
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //LogUtil.d("BaseActivity", getClass().getSimpleName());
        ActivityManager.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }
}
