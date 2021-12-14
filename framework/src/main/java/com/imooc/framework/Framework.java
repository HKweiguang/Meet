package com.imooc.framework;

import android.content.Context;

import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.utils.SpUtils;

import org.litepal.LitePal;

public class Framework {

    private volatile static Framework mFramework;

    private Framework() {

    }

    public static Framework getFramework() {
        if (mFramework == null) {
            synchronized (Framework.class) {
                if (mFramework == null) {
                    mFramework = new Framework();
                }
            }
        }

        return mFramework;
    }

    /**
     * 初始化框架 Model
     *
     * @param mContext
     */
    public void initFramework(Context mContext) {
        LogUtils.i("initFramework");
        SpUtils.getInstance().initSp(mContext);
        BmobManager.getInstance().initBmob(mContext);
        CloudManager.getInstance().initCloud(mContext);
        LitePal.initialize(mContext);
//        MapManager.getInstance().initMap(mContext);
//        WindowHelper.getInstance().initWindow(mContext);
//        CrashReport.initCrashReport(mContext, BUGLY_KEY, BuildConfig.LOG_DEBUG);
//        ZXingLibrary.initDisplayOpinion(mContext);
//        NotificationHelper.getInstance().createChannel(mContext);
//        KeyWordManager.getInstance().initManager(mContext);
//
//        //全局捕获RxJava异常
//        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                LogUtils.e("RxJava：" + throwable.toString());
//            }
//        });
    }
}
