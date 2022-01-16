package com.imooc.framework.helper;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class WindowHelper {
    private static volatile WindowHelper mInstance;

    private WindowHelper() {
    }

    public static WindowHelper getInstance() {
        if (mInstance == null) {
            synchronized (WindowHelper.class) {
                if (mInstance == null) {
                    mInstance = new WindowHelper();
                }
            }
        }

        return mInstance;
    }

    private Context context;
    private WindowManager wm;
    private WindowManager.LayoutParams layoutParams;

    //Handler
    private final Handler mHandler = new Handler();

    public void initWindow(Context context) {
        this.context = context;
        wm = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();

        // 设置宽高
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        // 设置标志位
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        // 设置格式
        layoutParams.format = PixelFormat.TRANSLUCENT;

        // 设置位置
        layoutParams.gravity = Gravity.CENTER;

        // 设置类型
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    /**
     * 创建View视图
     *
     * @param layoutId 资源id
     * @return view
     */
    public View getView(int layoutId) {
        return View.inflate(context, layoutId, null);
    }

    /**
     * 显示窗口
     *
     * @param view view
     */
    public void showView(final View view) {
        if (view != null) {
            if (view.getParent() == null) {
                mHandler.post(() -> {
                    try {
                        wm.addView(view, layoutParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * 自定义属性
     *
     * @param view view
     * @param layoutParams 布局
     */
    public void showView(final View view, final WindowManager.LayoutParams layoutParams) {
        if (view != null) {
            if (view.getParent() == null) {
                mHandler.post(() -> {
                    try {
                        wm.addView(view, layoutParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * 隐藏视图
     *
     * @param view view
     */
    public void hideView(View view) {
        if (view != null) {
            if (view.getParent() != null) {
                mHandler.post(() -> wm.removeView(view));
            }
        }
    }

    /**
     * 更新View的布局
     *
     * @param view view
     * @param layoutParams 布局
     */
    public void updateView(final View view, final WindowManager.LayoutParams layoutParams) {
        if (view != null && layoutParams != null) {
            mHandler.post(() -> wm.updateViewLayout(view, layoutParams));
        }
    }
}
