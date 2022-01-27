package com.imooc.meet.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import com.imooc.framework.manager.MediaPlayerManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.utils.SpUtils;
import com.imooc.framework.utils.TimeUtils;
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

    // 计时
    private static final int H_TIME_WHAT = 1000;

    // 通话时间
    private int callTimer = 0;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case H_TIME_WHAT:
                    callTimer++;
                    String time = TimeUtils.formatDuring(callTimer * 1000);
                    audio_tv_status.setText(time);
                    mHandler.sendEmptyMessageDelayed(H_TIME_WHAT, H_TIME_WHAT);
                    break;
            }
            return false;
        }
    });

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

    // 通话ID
    String callId = "";

    private MediaPlayerManager mAudioCallMedia;
    private MediaPlayerManager mAudioHangupMedia;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initService();
        initWindow();
        linkCloudServer();
    }

    /**
     * 初始化服务
     */
    private void initService() {
        // 来电铃声
        mAudioCallMedia = new MediaPlayerManager();
        // 挂断
        mAudioHangupMedia = new MediaPlayerManager();

        mAudioCallMedia.setOnComplteionListener(mp -> mAudioCallMedia.startPlay(CloudManager.callAudioPath));
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

        // 监听通话(收到电话)
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession callSession) {
                LogUtils.i("onReceivedCall");

                // 检查设备可用
                if (!CloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                    return;
                }

                // 呼叫端的ID
                String callerUserId = callSession.getCallerUserId();
                updateWindowInfo(0, callerUserId);

                // 通话ID
                callId = callSession.getCallId();

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
                LogUtils.i("onCallOutgoing");

                // 更新信息
                String targetId = callSession.getTargetId();
                updateWindowInfo(1, targetId);

                // 通话ID
                callId = callSession.getCallId();

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
                LogUtils.i("onCallConnected");

                // 关闭铃声
                if (mAudioCallMedia.isPlaying()) {
                    mAudioCallMedia.stopPlay();
                }

                // 开始计时
                mHandler.sendEmptyMessage(H_TIME_WHAT);

                // 更新按钮
                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    goneAudioView(true, false, true, true, true);
                } else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {

                }
            }

            // 通话结束
            @Override
            public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
                LogUtils.i("onCallDisconnected");

                // 关闭计时
                mHandler.removeMessages(H_TIME_WHAT);

                // 铃声挂断
                mAudioCallMedia.pausePlay();

                // 播放挂断铃声
                mAudioHangupMedia.startPlay(CloudManager.callAudioHangup);

                // 重置计时器
                callTimer = 0;

                // 更新按钮
                if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    WindowHelper.getInstance().hideView(mFullAudioView);
                } else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {

                }
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

        if (index == 0) {
            goneAudioView(false, true, true, false, false);

            // 播放来电铃声
            mAudioCallMedia.startPlay(CloudManager.callAudioPath);
        } else if (index == 1) {
            goneAudioView(false, false, true, false, false);
        }

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

    /**
     * 隐藏View
     *
     * @param recording 录音
     * @param answer    接听
     * @param hangup    挂断
     * @param hf        免提
     * @param small     最小化
     */
    private void goneAudioView(boolean recording, boolean answer, boolean hangup, boolean hf, boolean small) {
        audio_ll_recording.setVisibility(recording ? View.VISIBLE : View.GONE);
        audio_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        audio_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        audio_ll_hf.setVisibility(hf ? View.VISIBLE : View.GONE);
        audio_iv_small.setVisibility(small ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private boolean isRecording = false;
    private boolean isHF = false;

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_ll_recording:
                if (isRecording) {
                    isRecording = false;
                    CloudManager.getInstance().stopAudioRecording();
                    audio_iv_recording.setImageResource(R.drawable.img_recording);
                } else {
                    isRecording = true;
                    // 录音
                    CloudManager.getInstance().startAudioRecording(
                            "/sdcard/Meet/" + System.currentTimeMillis() + "wav");
                    audio_iv_recording.setImageResource(R.drawable.img_recording_p);
                }
                break;
            case R.id.audio_ll_answer:
                // 接听
                CloudManager.getInstance().acceptCall(callId);
                break;
            case R.id.audio_ll_hangup:
                // 挂断
                CloudManager.getInstance().hangUpCall(callId);
                break;
            case R.id.audio_ll_hf:
                // 免提
                isHF = !isHF;
                CloudManager.getInstance().setEnableSpeakerphone(isHF);
                audio_iv_hf.setImageResource(isHF ? R.drawable.img_hf_p : R.drawable.img_hf);
                break;
            case R.id.audio_iv_small:
                // 最小化
                break;
        }
    }
}
