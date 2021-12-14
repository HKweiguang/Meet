package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.utils.SpUtils;
import com.imooc.framework.view.DialogView;
import com.imooc.framework.view.LoadingView;
import com.imooc.framework.view.TouchPictureV;
import com.imooc.meet.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

public class LoginActivity extends BaseUIActivity implements View.OnClickListener {

    private EditText et_phone;
    private EditText et_code;
    private Button btn_send_code;
    private TextView btn_login;

    private static final int H_TIME = 1001;
    private static int TIME = 60;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == H_TIME) {
                --TIME;
                btn_send_code.setText(TIME + "s");
                if (TIME > 0) {
                    mHandler.sendEmptyMessageDelayed(H_TIME, 1000);
                } else {
                    btn_send_code.setEnabled(true);
                    btn_send_code.setText(getString(R.string.text_login_send));
                }
            }
            return false;
        }
    });

    private DialogView mCodeView;
    private TouchPictureV mPictureV;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        if (BmobManager.getInstance().isLogin()) {
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        initDialogView();
    }

    private void initView() {
        et_phone = findViewById(R.id.et_phone);
        et_code = findViewById(R.id.et_code);
        btn_send_code = findViewById(R.id.btn_send_code);
        btn_login = findViewById(R.id.btn_login);

        btn_send_code.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        String phone = SpUtils.getInstance().getString(Constants.SP_PHONE, "");
        if (!TextUtils.isEmpty(phone)) {
            et_phone.setText(phone);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_code:
                DialogManager.getInstance().show(mCodeView);
                break;
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void initDialogView() {
        mLoadingView = new LoadingView(this);

        mCodeView = DialogManager.getInstance().initView(this, R.layout.dialog_code_view);
        mPictureV = mCodeView.findViewById(R.id.mPictureV);
        mPictureV.setViewResultListener(() -> {
            DialogManager.getInstance().hide(mCodeView);
            sendSMS();
        });
    }

    /**
     * 发送短信验证码
     */
    private void sendSMS() {
        String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.text_login_phone_null), Toast.LENGTH_SHORT).show();
            return;
        }

        BmobManager.getInstance().requestSMS(phone, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    btn_send_code.setEnabled(false);
                    mHandler.sendEmptyMessage(H_TIME);
                    Toast.makeText(LoginActivity.this, "短信验证码发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "短信验证码发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login() {
        String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.text_login_phone_null), Toast.LENGTH_SHORT).show();
            return;
        }

        String code = et_code.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.text_login_code_null), Toast.LENGTH_SHORT).show();
            return;
        }

        mLoadingView.show("正在登陆...");
        BmobManager.getInstance().signOrLoginByMobilePhone(phone, code, new LogInListener<IMUser>() {
            @Override
            public void done(IMUser imUser, BmobException e) {
                mLoadingView.hide();
                if (e == null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();

                    SpUtils.getInstance().putString(Constants.SP_PHONE, phone);
                } else {
                    if (e.getErrorCode() == 207) {
                        Toast.makeText(LoginActivity.this, getString(R.string.text_login_code_error), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(LoginActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
