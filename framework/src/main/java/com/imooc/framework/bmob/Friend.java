package com.imooc.framework.bmob;

import cn.bmob.v3.BmobObject;

public class Friend extends BmobObject {

    //我自己
    private IMUser user;
    //好友
    private IMUser friendUser;

    public IMUser getUser() {
        return user;
    }

    public void setUser(IMUser user) {
        this.user = user;
    }

    public IMUser getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(IMUser friendUser) {
        this.friendUser = friendUser;
    }
}
