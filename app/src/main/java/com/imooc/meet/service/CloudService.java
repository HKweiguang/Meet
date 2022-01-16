package com.imooc.meet.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.db.LitePalHelper;
import com.imooc.framework.db.NewFriend;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.event.EventManager;
import com.imooc.framework.event.MessageEvent;
import com.imooc.framework.gson.TextBean;
import com.imooc.framework.helper.GlideHelper;
import com.imooc.framework.helper.WindowHelper;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.utils.SpUtils;
import com.imooc.meet.R;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;
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

public class CloudService extends Service implements View.OnClickListener {

    private Disposable disposable;
    //音频窗口
    private View mFullAudioView;
    //头像
    private CircleImageView audio_iv_photo;
    //状态
    private TextView audio_tv_status;
    //录音图片
    private ImageView audio_iv_recording;
    //录音按钮
    private LinearLayout audio_ll_recording;
    //接听图片
    private ImageView audio_iv_answer;
    //接听按钮
    private LinearLayout audio_ll_answer;
    //挂断图片
    private ImageView audio_iv_hangup;
    //挂断按钮
    private LinearLayout audio_ll_hangup;
    //免提图片
    private ImageView audio_iv_hf;
    //免提按钮
    private LinearLayout audio_ll_hf;
    //最小化
    private ImageView audio_iv_small;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initWindow();
        linkCloudServer();
    }

    /**
     * 初始化窗口
     */
    private void initWindow() {
        mFullAudioView = WindowHelper.getInstance().getView(R.layout.layout_chat_audio);
        audio_iv_photo = mFullAudioView.findViewById(R.id.audio_iv_photo);
        audio_tv_status = mFullAudioView.findViewById(R.id.audio_tv_status);
        audio_iv_recording = mFullAudioView.findViewById(R.id.audio_iv_recording);
        audio_ll_recording = mFullAudioView.findViewById(R.id.audio_ll_recording);
        audio_iv_answer = mFullAudioView.findViewById(R.id.audio_iv_answer);
        audio_ll_answer = mFullAudioView.findViewById(R.id.audio_ll_answer);
        audio_iv_hangup = mFullAudioView.findViewById(R.id.audio_iv_hangup);
        audio_ll_hangup = mFullAudioView.findViewById(R.id.audio_ll_hangup);
        audio_iv_hf = mFullAudioView.findViewById(R.id.audio_iv_hf);
        audio_ll_hf = mFullAudioView.findViewById(R.id.audio_ll_hf);
        audio_iv_small = mFullAudioView.findViewById(R.id.audio_iv_small);

        audio_ll_recording.setOnClickListener(this);
        audio_ll_answer.setOnClickListener(this);
        audio_ll_hangup.setOnClickListener(this);
        audio_ll_hf.setOnClickListener(this);
        audio_iv_small.setOnClickListener(this);
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

                // 呼叫端的ID
                String callerUserId = callSession.getCallerUserId();
                updateWindowInfo(0, callerUserId);

                // 通话ID
                callSession.getCallId();

                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    LogUtils.i("音频通话");

                    WindowHelper.getInstance().showView(mFullAudioView);
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

            // 电话播出
            @Override
            public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
                String call = new Gson().toJson(callSession);
                LogUtils.i("onCallOutgoing:" + call);

                // 更新信息
                String targetId = callSession.getTargetId();
                updateWindowInfo(1, targetId);

                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    LogUtils.i("音频通话");

                    WindowHelper.getInstance().showView(mFullAudioView);
                }
                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    LogUtils.i("视频通话");
                }
            }

            // 已建立通话
            @Override
            public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
                String call = new Gson().toJson(callSession);
                LogUtils.i("onCallConnected:" + call);
            }

            // 通话结束
            @Override
            public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
                String call = new Gson().toJson(callSession);
                LogUtils.i("onCallDisconnected:" + call);
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

    /**
     * 更新窗口上的用户信息
     *
     * @param index 0:接收    1:拨打
     * @param id    ID
     */
    public void updateWindowInfo(int index, String id) {
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        IMUser imUser = list.get(0);
                        // 直接设置
                        GlideHelper.loadUrl(CloudService.this, imUser.getPhoto(), audio_iv_photo);
                        if (index == 0) {
                            audio_tv_status.setText(imUser.getNickName() + "来电了...");
                        } else if (index == 1) {
                            audio_tv_status.setText("正在呼叫" + imUser.getNickName() + "...");
                        }
                    }
                }
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

    @Override
    public void onClick(View v) {

    }
}
