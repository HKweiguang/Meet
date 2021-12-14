package com.imooc.framework.bmob;

import android.content.Context;

import com.imooc.framework.utils.CommonUtils;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class BmobManager {
    private static final String BMOB_SDK_ID = "4ebce0bbfa7cd13b9d48e62fb0930ffc";

    private static volatile BmobManager mInstance;

    private BmobManager() {
    }

    public static BmobManager getInstance() {
        if (mInstance == null) {
            synchronized (BmobManager.class) {
                if (mInstance == null) {
                    mInstance = new BmobManager();
                }
            }
        }

        return mInstance;
    }

    public void initBmob(Context mContext) {
        Bmob.initialize(mContext, BMOB_SDK_ID);
    }

    /**
     * 是否登录
     *
     * @return true-是   false-否
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }

    /**
     * 获取本地对象
     *
     * @return 本地对象
     */
    public IMUser getUser() {
        return BmobUser.getCurrentUser(IMUser.class);
    }

    /**
     * 发送短信验证法
     *
     * @param phone    手机号
     * @param listener 回调
     */
    public void requestSMS(String phone, QueryListener<Integer> listener) {
        BmobSMS.requestSMSCode(phone, "", listener);
    }

    /**
     * 通过手机号码注册或登录
     *
     * @param phone    手机号
     * @param code     验证码
     * @param listener 回调
     */
    public void signOrLoginByMobilePhone(String phone, String code, LogInListener<IMUser> listener) {
        BmobUser.signOrLoginByMobilePhone(phone, code, listener);
    }

    /**
     * 第一次上传头像
     *
     * @param nickName 昵称
     * @param file     照片
     * @param listener 事件监听
     */
    public void uploadFirstPhoto(String nickName, File file, OnUploadPhotoListener listener) {
        final IMUser user = getUser();

        BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    user.setNickName(nickName);
                    user.setPhoto(bmobFile.getFileUrl());

                    user.setTokenNickName(nickName);
                    user.setTokenPhoto(bmobFile.getFileUrl());

                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                listener.onUpdateDone();
                            } else {
                                listener.onUpdateFail(e);
                            }
                        }
                    });
                } else {
                    listener.onUpdateFail(e);
                }
            }
        });
    }

    public interface OnUploadPhotoListener {
        void onUpdateDone();

        void onUpdateFail(BmobException e);
    }

    /**
     * 根据电话号码查询用户
     *
     * @param phone 电话号码
     */
    public void queryPhoneUser(String phone, FindListener<IMUser> listener) {
        baseQuery("mobilePhoneNumber", phone, listener);
    }

    /**
     * 根据ObjectId查询用户
     *
     * @param objectId ObjectId
     */
    public void queryObjectIdUser(String objectId, FindListener<IMUser> listener) {
        baseQuery("objectId", objectId, listener);
    }

    /**
     * 查询我的好友
     *
     * @param listener 回调
     */
    public void queryMyFriend(FindListener<Friend> listener) {
        baseQuery("user", getUser(), listener);
    }

    /**
     * 查询所有的用户
     *
     * @param listener 回调
     */
    public void queryAllUser(FindListener<IMUser> listener) {
        baseQuery(null, null, listener);
    }

    /**
     * 查询基类
     *
     * @param key      列名
     * @param value    参数
     * @param listener 回调
     */
    public <T, V> void baseQuery(String key, V value, FindListener<T> listener) {
        BmobQuery<T> query = new BmobQuery<>();
        if (key != null && value != null) {
            query.addWhereEqualTo(key, value);
        }
        query.findObjects(listener);
    }

    /**
     * 添加好友
     */
    public void addFriend(IMUser imUser, SaveListener<String> listener) {
        Friend friend = new Friend();
        friend.setUser(getUser());
        friend.setFriendUser(imUser);
        friend.save(listener);
    }

    /**
     * 添加好友
     */
    public void addFriend(String id, SaveListener<String> listener) {
        queryObjectIdUser(id, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        IMUser imUser = list.get(0);
                        addFriend(imUser, listener);
                    }
                }
            }
        });
    }
}
