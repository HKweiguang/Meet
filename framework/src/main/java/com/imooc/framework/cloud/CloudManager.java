package com.imooc.framework.cloud;

import android.content.Context;
import android.net.Uri;

import com.imooc.framework.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
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
     * @param file 图片
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
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE, targetId, 0, 20, callback);
    }
}
