package com.imooc.framework.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.imooc.framework.R;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.utils.AnimUtils;

public class LoadingView {

    private DialogView mLoadingView;
    private ImageView iv_loding;
    private TextView tv_loding_text;

    private ObjectAnimator mAnim;

    public LoadingView(Context context) {
        mLoadingView = DialogManager.getInstance().initView(context, R.layout.dialog_loding);
        iv_loding = mLoadingView.findViewById(R.id.iv_loding);
        tv_loding_text = mLoadingView.findViewById(R.id.tv_loding_text);

        mAnim = AnimUtils.rotation(iv_loding);
    }

    public void setLoadingText(String text) {
        if (!TextUtils.isEmpty(text)) {
            tv_loding_text.setText(text);
        }
    }

    public void show() {
        mAnim.start();
        DialogManager.getInstance().show(mLoadingView);
    }

    public void show(String text) {
        setLoadingText(text);
        show();
    }

    public void hide() {
        mAnim.pause();
        DialogManager.getInstance().hide(mLoadingView);
    }
}
