package com.imooc.framework.manager;

import android.content.Context;
import android.view.Gravity;

import com.imooc.framework.R;
import com.imooc.framework.view.DialogView;

public class DialogManager {
    private static volatile DialogManager mInstance;

    private DialogManager() {
    }

    public static DialogManager getInstance() {
        if (mInstance == null) {
            synchronized (DialogManager.class) {
                if (mInstance == null) {
                    mInstance = new DialogManager();
                }
            }
        }

        return mInstance;
    }

    public DialogView initView(Context context, int layout) {
        return new DialogView(context, layout, R.style.Theme_Dialog, Gravity.CENTER);
    }

    public DialogView initView(Context context, int layout, int gravity) {
        return new DialogView(context, layout, R.style.Theme_Dialog, gravity);
    }

    public void show(DialogView view) {
        if (view != null) {
            if (!view.isShowing()) {
                view.show();
            }
        }
    }

    public void hide(DialogView view) {
        if (view != null) {
            if (view.isShowing()) {
                view.hide();
            }
        }
    }
}
