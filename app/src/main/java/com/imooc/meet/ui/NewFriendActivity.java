package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.db.LitePalHelper;
import com.imooc.framework.db.NewFriend;
import com.imooc.framework.event.EventManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.meet.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NewFriendActivity extends BaseBackActivity {

    private View item_empty_view;
    private RecyclerView mNewFriendView;

    private Disposable disposable;

    private CommonAdapter<NewFriend> newFriendCommonAdapter;
    private final ArrayList<NewFriend> mList = new ArrayList<>();

    private IMUser imUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        initView();
    }

    private void initView() {
        newFriendCommonAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<NewFriend>() {

            @Override
            public void onBindViewHolder(NewFriend model, CommonViewHolder holder, int position, int type) {
                // 根据id查询用户
                BmobManager.getInstance().queryObjectIdUser(model.getUserId(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            imUser = list.get(0);
                            holder.setImageUrl(NewFriendActivity.this, R.id.iv_photo, imUser.getPhoto());
                            holder.setImageResource(R.id.iv_sex, imUser.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                            holder.setText(R.id.tv_nickname, imUser.getNickName());
                            holder.setText(R.id.tv_age, imUser.getAge() + getString(R.string.text_search_age));
                            holder.setText(R.id.tv_desc, imUser.getDesc());
                            holder.setText(R.id.tv_msg, model.getMsg());

                            if (model.getIsAgree() == 0) {
                                holder.getView(R.id.ll_agree).setVisibility(View.GONE);
                                holder.getView(R.id.tv_result).setVisibility(View.VISIBLE);
                                holder.setText(R.id.tv_result, "已同意");
                            } else if (model.getIsAgree() == 1) {
                                holder.getView(R.id.ll_agree).setVisibility(View.GONE);
                                holder.getView(R.id.tv_result).setVisibility(View.VISIBLE);
                                holder.setText(R.id.tv_result, "已拒绝");
                            }
                        }
                    }
                });

                // 同意
                holder.getView(R.id.ll_yes).setOnClickListener(v -> {
                    updateItem(position, 0);
                    BmobManager.getInstance().addFriend(imUser, new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                CloudManager.getInstance().sendTextMessage("", CloudManager.TYPE_ARGEED_FRIEND, imUser.getObjectId());
                                EventManager.post(EventManager.FLAG_UPDATE_FRIEND_LIST);
                            }
                        }
                    });
                });

                // 拒绝
                holder.getView(R.id.ll_no).setOnClickListener(v -> updateItem(position, 1));
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_new_friend_item;
            }
        });

        item_empty_view = findViewById(R.id.item_empty_view);
        mNewFriendView = findViewById(R.id.mNewFriendView);
        mNewFriendView.setLayoutManager(new LinearLayoutManager(this));
        mNewFriendView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mNewFriendView.setAdapter(newFriendCommonAdapter);

        queryNewFriend();
    }

    /**
     * 更新Item
     *
     * @param position 位于数组下标
     * @param i        0:同意;  1:拒绝
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateItem(int position, int i) {
        NewFriend newFriend = mList.get(position);
        newFriend.setIsAgree(i);
        LitePalHelper.getInstance().updateNewFriend(newFriend.getUserId(), newFriend.getIsAgree());
        mList.set(position, newFriend);
        newFriendCommonAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void queryNewFriend() {
        disposable = Observable.create((ObservableOnSubscribe<List<NewFriend>>) emitter -> {
            emitter.onNext(LitePalHelper.getInstance().queryNewFriend());
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newFriends -> {
                    if (CommonUtils.isNotEmpty(newFriends)) {
                        mList.addAll(newFriends);
                        newFriendCommonAdapter.notifyDataSetChanged();
                    } else {
                        item_empty_view.setVisibility(View.VISIBLE);
                        mNewFriendView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
