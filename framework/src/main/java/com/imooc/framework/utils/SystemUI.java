package com.imooc.framework.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

public class SystemUI {

    @SuppressLint("ObsoleteSdkInt")
    public static void fixSystemUI(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            mActivity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
