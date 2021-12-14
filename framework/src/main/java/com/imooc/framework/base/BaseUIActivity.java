package com.imooc.framework.base;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.imooc.framework.utils.SystemUI;

public class BaseUIActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUI.fixSystemUI(this);
    }
}
