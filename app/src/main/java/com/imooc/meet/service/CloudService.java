package com.imooc.meet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

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

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
                    ImageMessage imageMessage = (ImageMessage) message.getContent();
                    String url = imageMessage.getRemoteUri().toString();
                    if (!TextUtils.isEmpty(url)) {
                        MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                        event.setImgUrl(url);
                        event.setUserId(message.getSenderUserId());
                        EventManager.post(event);
                    }
                    break;
                case CloudManager.MSG_LOCATION_NAME:

                    break;
            }
            return false;
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
