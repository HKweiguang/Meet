package com.imooc.meet.fragment.chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseFragment;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.bmob.Friend;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.meet.R;
import com.imooc.meet.model.AllFriendModel;
import com.imooc.meet.ui.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class AllFriendFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private View item_empty_view;
    private RecyclerView mAllFriendView;
    private SwipeRefreshLayout mAllFriendRefreshLayout;

    private CommonAdapter<AllFriendModel> allFriendModelCommonAdapter;
    private ArrayList<AllFriendModel> mList = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView(inflater.inflate(R.layout.fragment_all_record, null));
    }

    private View initView(View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mAllFriendRefreshLayout = view.findViewById(R.id.mAllFriendRefreshLayout);
        mAllFriendView = view.findViewById(R.id.mAllFriendView);

        mAllFriendRefreshLayout.setOnRefreshListener(this);

        allFriendModelCommonAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<AllFriendModel>() {
            @Override
            public void onBindViewHolder(AllFriendModel model, CommonViewHolder holder, int position, int type) {
                holder.setImageUrl(getActivity(), R.id.iv_photo, model.getUrl());
                holder.setText(R.id.tv_nickname, model.getNickName());
                holder.setImageResource(R.id.iv_sex, model.isSex()
                        ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                holder.setText(R.id.tv_desc, model.getDesc());

                holder.itemView.setOnClickListener(v -> UserInfoActivity.startActivity(getActivity(), mList.get(position).getUserId()));
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_all_friend_item;
            }
        });
        mAllFriendView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAllFriendView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        mAllFriendView.setAdapter(allFriendModelCommonAdapter);

        queryMyFriends();

        return view;
    }

    /**
     * 查询所有好友
     */
    private void queryMyFriends() {
        mAllFriendRefreshLayout.setRefreshing(true);
        BmobManager.getInstance().queryMyFriend(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                mAllFriendRefreshLayout.setRefreshing(false);
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        item_empty_view.setVisibility(View.GONE);
                        mAllFriendView.setVisibility(View.VISIBLE);

                        if (mList.size() > 0) {
                            mList.clear();
                        }

                        for (Friend friend :
                                list) {
                            String id = friend.getFriendUser().getObjectId();
                            BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void done(List<IMUser> list, BmobException e) {
                                    if (e == null) {
                                        if (CommonUtils.isNotEmpty(list)) {
                                            IMUser imUser = list.get(0);
                                            AllFriendModel model = new AllFriendModel();
                                            model.setUserId(imUser.getObjectId());
                                            model.setUrl(imUser.getPhoto());
                                            model.setNickName(imUser.getNickName());
                                            model.setSex(imUser.isSex());
                                            model.setDesc(imUser.getDesc());

                                            mList.add(model);
                                            allFriendModelCommonAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        item_empty_view.setVisibility(View.VISIBLE);
                        mAllFriendView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mAllFriendRefreshLayout.isRefreshing()) {
            queryMyFriends();
        }
    }
}
