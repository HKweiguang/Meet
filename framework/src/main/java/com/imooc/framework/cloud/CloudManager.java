package com.imooc.framework.cloud;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.imooc.framework.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class CloudManager {
    // Url
    public static final String TOKEN_URL = "https://api-cn.ronghub.com/user/getToken.json";
    // Key
    public static final String CLOUD_KEY = "x18ywvqfxyjxc";
    // Secret
    public static final String CLOUD_SECRET = "bdF7dsdybT";

    public static final String MSG_TEXT_NAME = "RC:TxtMsg";
    public static final String MSG_IMAGE_NAME = "RC:ImgMsg";
    public static final String MSG_LOCATION_NAME = "RC:LBSMsg";

    // 普通消息
    public static final String TYPE_TEXT = "TYPE_TEXT";
    // 添加好友消息
    public static final String TYPE_ADD_FRIEND = "TYPE_ADD_FRIEND";
    // 同意添加好友的消息
    public static final String TYPE_ARGEED_FRIEND = "TYPE_ARGEED_FRIEND";

    //来电铃声
    public static final String callAudioPath = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5363.wav";
    //挂断铃声
    public static final String callAudioHangup = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5351.wav";

    private static volatile CloudManager mInstance = null;

    private CloudManager() {

    }

    public static CloudManager getInstance() {
        if (mInstance == null) {
            synchronized (CloudManager.class) {
                if (mInstance == null) {
                    mInstance = new CloudManager();
                }
            }
        }

        return mInstance;
    }

    /**
     * 初始化SDK
     */
    public void initCloud(Context context) {
        RongIMClient.init(context, CLOUD_KEY);
    }

    /**
     * 连接融云服务
     */
    public void connect(String token) {
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                LogUtils.i("连接成功: " + s);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {
                LogUtils.e("连接失败: " + connectionErrorCode);
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {

            }
        });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        RongIMClient.getInstance().disconnect();
    }

    /**
     * 退出登录
     */
    public void logout() {
        RongIMClient.getInstance().logout();
    }

    /**
     * 接收消息的监听器
     *
     * @param listener 回调
     */
    public void setOnReceiveMessageListener(RongIMClient.OnReceiveMessageListener listener) {
        RongIMClient.setOnReceiveMessageListener(listener);
    }

    /**
     * 发送消息回调
     */
    private final IRongCallback.ISendMessageCallback iSendMessageCallback = new IRongCallback.ISendMessageCallback() {
        @Override
        public void onAttached(Message message) {
            // 消息成功存到本地数据库的回调
        }

        @Override
        public void onSuccess(Message message) {
            // 消息发送成功的回调
            LogUtils.i("消息发送成功: " + message);
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            // 消息发送失败的回调
            LogUtils.i("消息发送失败: " + errorCode);
        }
    };

    /**
     * 发送文本消息
     *
     * @param msg      消息内容
     * @param targetId 目标用户
     */
    public void sendTextMessage(String msg, String targetId) {
        TextMessage textMessage = TextMessage.obtain(msg);
        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE,
                targetId,
                textMessage,
                null,
                null,
                iSendMessageCallback);
    }

    /**
     * 发送文本消息 json
     */
    public void sendTextMessage(String msg, String type, String targetId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", msg);
            jsonObject.put("type", type);
            sendTextMessage(jsonObject.toString(), targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final RongIMClient.SendImageMessageCallback sendImageMessageCallback = new RongIMClient.SendImageMessageCallback() {
        @Override
        public void onAttached(Message message) {
            LogUtils.i("onAttached");
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            LogUtils.i("onError: " + errorCode);
        }

        @Override
        public void onSuccess(Message message) {
            LogUtils.i("onSuccess");
        }

        @Override
        public void onProgress(Message message, int i) {
            LogUtils.i("onProgress: " + i);
        }
    };

    /**
     * 发送图片消息
     *
     * @param file     图片
     * @param targetId 目标
     */
    public void sendImageMessage(File file, String targetId) {
        ImageMessage imageMessage = ImageMessage.obtain(Uri.fromFile(file), Uri.fromFile(file), true);
        RongIMClient.getInstance().sendImageMessage(
                Conversation.ConversationType.PRIVATE,
                targetId,
                imageMessage,
                null,
                null,
                sendImageMessageCallback
        );
    }

    /**
     * 发送位置信息
     *
     * @param mTargetId 目标
     * @param lat       经度
     * @param lng       纬度
     * @param poi       描述
     */
    public void sendLocationMessage(String mTargetId, double lat, double lng, String poi) {
        LocationMessage locationMessage = LocationMessage.obtain(lat, lng, poi, null);
        io.rong.imlib.model.Message message = io.rong.imlib.model.Message.obtain(
                mTargetId, Conversation.ConversationType.PRIVATE, locationMessage);
        RongIMClient.getInstance().sendLocationMessage(message,
                null, null, iSendMessageCallback);
    }

    /**
     * 查询本地会话记录
     */
    public void getConversationList(RongIMClient.ResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(callback);
    }

    /**
     * 加载本地的历史记录
     *
     * @param targetId 目标id
     * @param callback 回调
     */
    public void getHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, targetId, -1, 1000, callback);
    }

    /**
     * 加载服务器的历史记录
     *
     * @param targetId 目标id
     * @param callback 回调
     */
    public void getRemoteHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE
                , targetId, 0, 20, callback);
    }

    /**
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 拨打音频、视频电话
     *
     * @param targetId 目标id
     * @param type     类型
     */
    public void startCall(String targetId, RongCallCommon.CallMediaType type) {
        List<String> userIds = new ArrayList<>();
        userIds.add(targetId);
        RongCallClient.getInstance().startCall(Conversation.ConversationType.PRIVATE, targetId, userIds, null, type, null);
    }

    public void startAudioCall(Context mContext, String targetId) {
        // 检查设备可用
        if (!isVoIPEnabled(mContext)) {
            return;
        }
        startCall(targetId, RongCallCommon.CallMediaType.AUDIO);
    }

    public void startVideoCall(Context mContext, String targetId) {
        // 检查设备可用
        if (!isVoIPEnabled(mContext)) {
            return;
        }
        startCall(targetId, RongCallCommon.CallMediaType.VIDEO);
    }

    /**
     * 监听音视频
     *
     * @param listener 回调
     */
    public void setReceivedCallListener(IRongReceivedCallListener listener) {
        if (listener == null) {
            return;
        }
        RongCallClient.setReceivedCallListener(listener);
    }

    /**
     * 接听
     *
     * @param callId 目标id
     */
    public void acceptCall(String callId) {
        RongCallClient.getInstance().acceptCall(callId);
    }

    /**
     * 挂断
     *
     * @param callId 目标id
     */
    public void hangUpCall(String callId) {
        RongCallClient.getInstance().hangUpCall(callId);
    }

    /**
     * 切换媒体
     *
     * @param mediaType 媒体类型
     */
    public void changeCallMediaType(RongCallCommon.CallMediaType mediaType) {
        RongCallClient.getInstance().changeCallMediaType(mediaType);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        RongCallClient.getInstance().switchCamera();
    }

    /**
     * 摄像头开关
     *
     * @param enabled 开关
     */
    public void setEnableLocalVideo(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalVideo(enabled);
    }

    /**
     * 音频开关
     *
     * @param enabled 开关
     */
    public void setEnableLocalAudio(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalAudio(enabled);
    }

    /**
     * 免提开关
     *
     * @param enabled 开关
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RongCallClient.getInstance().setEnableSpeakerphone(enabled);
    }

    /**
     * 开启录音
     *
     * @param filePath
     */
    public void startAudioRecording(String filePath) {
    }

    /**
     * 关闭录音
     */
    public void stopAudioRecording() {
    }

    /**
     * 监听通话状态
     *
     * @param listener 回调
     */
    public void setVoIPCallListener(IRongCallListener listener) {
        if (listener == null) {
            return;
        }
        RongCallClient.getInstance().setVoIPCallListener(listener);
    }

    /**
     * 检查设备是否可通话
     *
     * @param context 上下文
     */
    public boolean isVoIPEnabled(Context context) {
        if (RongCallClient.getInstance().isVoIPEnabled(context)) {
            Toast.makeText(context, "设备不支持音视频通话", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
