package com.imooc.framework.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.imooc.framework.event.EventManager;
import com.imooc.framework.event.MessageEvent;
import com.imooc.framework.utils.LanguaueUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    //申请运行时权限的Code
    protected static final int PERMISSION_REQUEST_CODE = 1000;
    //申请窗口权限的Code
    public static final int PERMISSION_WINDOW_REQUEST_CODE = 1001;

    /**
     * 声明所需权限
     */
    private final String[] mStrPermission = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    /**
     * 保存没有同意权限
     */
    private final ArrayList<String> mPerList = new ArrayList<>();
    /**
     * 保存没有同意的失败权限
     */
    private final ArrayList<String> mPerNoList = new ArrayList<>();

    private int requestCode;

    private OnPermissionResult permissionResult;

    protected void request(int requestCode, OnPermissionResult permissionResult) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermissionAll()) {
            if (!checkPermissionAll()) {
                requestPermissionAll(requestCode, permissionResult);
            }
        }
    }

    /**
     * 判断单个权限
     *
     * @param permissions 权限名称
     * @return true-false
     */
    protected boolean checkPermission(String permissions) {
        return ContextCompat.checkSelfPermission(this, permissions) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断是否需要申请权限
     *
     * @return true-false
     */
    protected boolean checkPermissionAll() {
        mPerList.clear();
        for (String per : mStrPermission) {
            if (!checkPermission(per)) {
                mPerList.add(per);
            }
        }
        return !(mPerList.size() > 0);
    }

    /**
     * 请求权限
     *
     * @param mPermissions 权限
     */
    protected void requestPermission(String[] mPermissions) {
        ActivityCompat.requestPermissions(this, mPermissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * 请求权限
     *
     * @param mPermissions 权限
     * @param requestCode  请求代码
     */
    protected void requestPermission(String[] mPermissions, int requestCode) {
        ActivityCompat.requestPermissions(this, mPermissions, requestCode);
    }

    /**
     * 申请所有权限
     *
     * @param requestCode 请求代码
     */
    protected void requestPermissionAll(int requestCode, OnPermissionResult permissionResult) {
        this.requestCode = requestCode;
        this.permissionResult = permissionResult;
        requestPermission(mPerList.toArray(new String[0]), requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == this.requestCode) {
            mPerNoList.clear();
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        mPerNoList.add(permissions[i]);
                    }
                }
                if (permissionResult != null) {
                    if (mPerNoList.size() == 0) {
                        permissionResult.onSuccess();
                    } else {
                        permissionResult.onFail(mPerNoList);
                    }
                }
                Toast.makeText(this, "申请成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "申请失败", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected interface OnPermissionResult {
        void onSuccess();

        void onFail(ArrayList<String> noPermissions);
    }

    /**
     * 判断窗口权限
     *
     * @return true-false
     */
    protected boolean checkWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    /**
     * 请求窗口权限
     *
     * @param requestCode 请求代码
     */
    protected void requestWindowPermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventManager.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getType() == EventManager.EVENT_RUPDATE_LANGUAUE) {
            LanguaueUtils.updateLanguaue(this);
            recreate();
        }
    }

}
