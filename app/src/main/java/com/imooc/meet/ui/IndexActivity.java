package com.imooc.meet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.utils.SpUtils;
import com.imooc.meet.R;

public class IndexActivity extends BaseUIActivity {

    private static final int SKIP_MAIN = 1000;

    private final Handler mHandler = new Handler(msg -> {
        if (msg.what == SKIP_MAIN) {
            startMain();
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        mHandler.sendEmptyMessageDelayed(SKIP_MAIN, 2 * 1000);
    }

    /**
     * 进入主页
     */
    private void startMain() {

//        Intent intent = new Intent();
//        intent.setClass(this, GuideActivity.class);
//        startActivity(intent);
//        finish();

        boolean isFirst = SpUtils.getInstance().getBoolean(Constants.SP_IS_FIRST_APP, true);
        Intent intent = new Intent();
        if (isFirst) {
            intent.setClass(this, GuideActivity.class);
            SpUtils.getInstance().putBoolean(Constants.SP_IS_FIRST_APP, false);
        } else {
            String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
            if (TextUtils.isEmpty(token)) {
                if (BmobManager.getInstance().isLogin()) {
                    intent.setClass(this, MainActivity.class);
                } else {
                    intent.setClass(this, LoginActivity.class);
                }
            } else {
                intent.setClass(this, MainActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }

}
