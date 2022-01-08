package com.imooc.framework.manager;

import android.content.Context;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.imooc.framework.utils.LogUtils;

public class VoiceManager {
    private static volatile VoiceManager mInstance = null;

    private VoiceManager(Context mContext) {
        initVoice(mContext);
    }

    public static VoiceManager getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (VoiceManager.class) {
                if (mInstance == null) {
                    mInstance = new VoiceManager(mContext);
                }
            }
        }

        return mInstance;
    }

    private RecognizerDialog mIatDialog;

    public void initVoice(Context mContext) {
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=7874f451");

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(mContext, i -> LogUtils.i("InitListener:" + i));

        //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIatDialog.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIatDialog.setParameter(SpeechConstant.SUBJECT, null);
        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置语音输入语言，zh_cn为简体中文
        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mIatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIatDialog.setParameter(SpeechConstant.ASR_PTT, "1");
    }

    public void startSpeak(RecognizerDialogListener listener) {
        mIatDialog.setListener(listener);
        mIatDialog.show();
    }
}
