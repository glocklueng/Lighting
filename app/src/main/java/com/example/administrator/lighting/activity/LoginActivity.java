package com.example.administrator.lighting.activity;

import com.example.administrator.lighting.R;
import com.example.administrator.lighting.manager.BaseActivity;
import com.example.administrator.lighting.util.LogUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {

    private final static String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final TextInputLayout wrapperUsername = (TextInputLayout) findViewById(R.id.id_username_wrapper);
        final EditText edUsername = wrapperUsername.getEditText();
        final TextInputLayout wrapperPassword = (TextInputLayout) findViewById(R.id.id_password_wrapper);
        final EditText edPassword = wrapperPassword.getEditText();
        Button btnLogin = (Button) findViewById(R.id.id_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "用户名：" + edUsername.getText());
                LogUtil.d(TAG, "密码：" + edPassword.getText());
                if (edUsername.getText().toString().equals("d50912") && edPassword.getText().toString().equals("d50912")) {
                    wrapperPassword.setErrorEnabled(false);
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    wrapperPassword.setError("用户名或密码错误！");
                    wrapperPassword.setErrorEnabled(true);
                }
            }
        });
    }
}
