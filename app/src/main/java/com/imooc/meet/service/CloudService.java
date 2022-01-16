package com.imooc.meet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.google.gson.Gson;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.db.LitePalHelper;
import com.imooc.framework.db.NewFriend;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.event.EventManager;
import com.imooc.framework.event.MessageEvent;
import com.imooc.framework.gson.TextBean;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.utils.SpUtils;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class CloudService extends Service {

    private Disposable disposable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        linkCloudServer();
    }

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        LogUtils.i("token: " + token);
        // 连接服务
        CloudManager.getInstance().connect(token);
        // 接收消息
        CloudManager.getInstance().setOnReceiveMessageListener((message, i) -> {
            LogUtils.i("message: " + message);
            switch (message.getObjectName()) {
                case CloudManager.MSG_TEXT_NAME:
                    TextMessage textMessage = (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    LogUtils.i("content: " + content);
                    TextBean textBean = new Gson().fromJson(content, TextBean.class);
                    switch (textBean.getType()) {
                        case CloudManager.TYPE_TEXT:
                            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
                            event.setText(textBean.getMsg());
                            event.setUserId(message.getSenderUserId());
                            EventManager.post(event);
                            break;
                        case CloudManager.TYPE_ADD_FRIEND:
                            LogUtils.i("添加好友消息");

                            disposable = Observable.create((ObservableOnSubscribe<List<NewFriend>>) emitter -> {
                                emitter.onNext(LitePalHelper.getInstance().queryNewFriend());
                                emitter.onComplete();
                            }).subscribeOn(Schedulers.newThread()).
                                    observeOn(AndroidSchedulers.mainThread()).subscribe(newFriends -> {
                                boolean isHave = false;
                                if (CommonUtils.isNotEmpty(newFriends)) {
                                    for (NewFriend friend : newFriends) {
                                        if (message.getSenderUserId().equals(friend.getUserId())) {
                                            isHave = true;
                                            break;
                                        }
                                    }

                                    if (!isHave) {
                                        LitePalHelper.getInstance().saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                                    }
                                } else {
                                    LitePalHelper.getInstance().saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                                }
                            });
                            break;
                        case CloudManager.TYPE_ARGEED_FRIEND:
                            BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e == null) {
                                        EventManager.post(EventManager.FLAG_UPDATE_FRIEND_LIST);
                                    }
                                }
                            });
                            break;
                    }
                    break;
                case CloudManager.MSG_IMAGE_NAME:
                    try {
                        ImageMessage imageMessage = (ImageMessage) message.getContent();
                        String url = imageMessage.getRemoteUri().toString();
                        if (!TextUtils.isEmpty(url)) {
                            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                            event.setImgUrl(url);
                            event.setUserId(message.getSenderUserId());
                            EventManager.post(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case CloudManager.MSG_LOCATION_NAME:
                    LocationMessage locationMessage = (LocationMessage) message.getContent();
                    MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_LOCATION);
                    event.setLa(locationMessage.getLat());
                    event.setLo(locationMessage.getLng());
                    event.setAddress(locationMessage.getPoi());
                    event.setUserId(message.getSenderUserId());
                    EventManager.post(event);
                    break;
            }
            return false;
        });

        // 监听通话
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession callSession) {
                LogUtils.i("onReceivedCall:" + callSession.toString());
                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    LogUtils.i("音频通话");
                }
                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    LogUtils.i("视频通话");
                }
            }

            @Override
            public void onCheckPermission(RongCallSession callSession) {
                LogUtils.i("onCheckPermission:" + callSession.toString());
            }
        });

        // 监听通话状态
        CloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {
            @Override
            public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {

            }

            @Override
            public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {

            }

            @Override
            public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {

            }

            @Override
            public void onRemoteUserRinging(String userId) {

            }

            @Override
            public void onRemoteUserAccept(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {

            }

            @Override
            public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {

            }

            @Override
            public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {

            }

            @Override
            public void onError(RongCallCommon.CallErrorCode errorCode) {

            }

            @Override
            public void onRemoteCameraDisabled(String userId, boolean disabled) {

            }

            @Override
            public void onRemoteMicrophoneDisabled(String userId, boolean disabled) {

            }

            @Override
            public void onNetworkReceiveLost(String userId, int lossRate) {

            }

            @Override
            public void onNetworkSendLost(int lossRate, int delay) {

            }

            @Override
            public void onFirstRemoteVideoFrame(String userId, int height, int width) {

            }

            @Override
            public void onAudioLevelSend(String audioLevel) {

            }

            @Override
            public void onAudioLevelReceive(HashMap<String, String> audioLevel) {

            }

            @Override
            public void onRemoteUserPublishVideoStream(String userId, String streamId, String tag, SurfaceView surfaceView) {

            }

            @Override
            public void onRemoteUserUnpublishVideoStream(String userId, String streamId, String tag) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
