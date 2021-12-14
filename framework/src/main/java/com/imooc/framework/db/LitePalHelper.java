package com.imooc.framework.db;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

public class LitePalHelper {

    private static volatile LitePalHelper mInstance = null;

    private LitePalHelper() {
    }

    public static LitePalHelper getInstance() {
        if (mInstance == null) {
            synchronized (LitePalHelper.class) {
                if (mInstance == null) {
                    mInstance = new LitePalHelper();
                }
            }
        }

        return mInstance;
    }

    public void baseSave(LitePalSupport support) {
        support.save();
    }

    public <T extends LitePalSupport> List<T> baseQuery(Class<T> cls) {
        return LitePal.findAll(cls);
    }

    public void saveNewFriend(String msg, String userId) {
        NewFriend newFriend = new NewFriend();
        newFriend.setMsg(msg);
        newFriend.setUserId(userId);
        newFriend.setIsAgree(-1);
        newFriend.setSaveTime(System.currentTimeMillis());
        baseSave(newFriend);
    }

    public List<NewFriend> queryNewFriend() {
        return baseQuery(NewFriend.class);
    }

    public void updateNewFriend(String userId, int agree) {
        NewFriend newFriend = new NewFriend();
        newFriend.setIsAgree(agree);
        newFriend.updateAll("userId = ?", userId);
    }
}
